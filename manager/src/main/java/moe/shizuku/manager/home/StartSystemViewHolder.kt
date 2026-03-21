package moe.shizuku.manager.home

import moe.shizuku.manager.patch.Util
import android.content.Intent
import android.text.method.LinkMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import moe.shizuku.manager.Helps
import moe.shizuku.manager.R
import moe.shizuku.manager.databinding.HomeItemContainerBinding
import moe.shizuku.manager.databinding.HomeStartSystemBinding
import moe.shizuku.manager.ktx.toHtml
import moe.shizuku.manager.starter.StarterActivity
import rikka.html.text.HtmlCompat
import rikka.recyclerview.BaseViewHolder
import rikka.recyclerview.BaseViewHolder.Creator
import rikka.shizuku.Shizuku

class StartSystemViewHolder(private val binding: HomeStartSystemBinding, root: View) :
    BaseViewHolder<Boolean>(root) {

    companion object {
        val CREATOR = Creator<Boolean> { inflater: LayoutInflater, parent: ViewGroup? ->
            val outer = HomeItemContainerBinding.inflate(inflater, parent, false)
            val inner = HomeStartSystemBinding.inflate(inflater, outer.root, true)
            StartSystemViewHolder(inner, outer.root)
        }
    }

    private inline val start get() = binding.button1
    private inline val restart get() = binding.button2

    private var alertDialog: AlertDialog? = null

    init {
        val listener = View.OnClickListener { v: View -> onStartClicked(v) }
        start.setOnClickListener(listener)
        restart.setOnClickListener(listener)
        binding.text1.movementMethod = LinkMovementMethod.getInstance()
    }

    private fun onStartClicked(v: View) {
        val context = v.context
        val intent = Intent(context, StarterActivity::class.java).apply {
            putExtra(StarterActivity.EXTRA_IS_ROOT, false)
            putExtra(StarterActivity.EXTRA_IS_SYSTEM, true)
        }
        context.startActivity(intent)
    }

    override fun onBind() {
        start.isEnabled = true
        restart.isEnabled = true
        if (data!!) {
            start.visibility = View.GONE
            restart.visibility = View.VISIBLE
        } else {
            start.visibility = View.VISIBLE
            restart.visibility = View.GONE
        }

        val sb = StringBuilder()
            .append(
                context.getString(
                    R.string.home_system_description
                )
            )
            
        if (!Util.canUsePoc()) {
            sb.append("<p>").sb.append(
                context.getString(
                    R.string.home_system_description_warn
                )
            )
        }

        binding.text1.text = sb.toString()
    }

    override fun onRecycle() {
        super.onRecycle()
        alertDialog = null
    }
}
