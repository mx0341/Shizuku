#!/bin/bash

# 切换到项目目录（按实际位置调整）
cd ~/Shizuku || exit

# 设置默认提交信息（可随命令行参数自定义）
MSG="${1:-更新代码}"

# 查看当前状态
echo "======== 当前改动 ========"
git status

# 确认是否继续
read -p "是否提交并推送？(y/n) " confirm
if [[ "$confirm" != "y" && "$confirm" != "Y" ]]; then
    echo "已取消"
    exit 0
fi

# 添加所有改动
git add -A

# 提交
git commit -m "$MSG"

# 推送（分支默认 master，如果你的仓库用 main 请自行替换）
git push origin master

echo "======== 推送完成 ========"