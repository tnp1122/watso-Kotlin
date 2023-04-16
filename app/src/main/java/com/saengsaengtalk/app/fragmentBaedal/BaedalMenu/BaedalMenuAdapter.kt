package com.saengsaengtalk.app.fragmentBaedal.BaedalMenu

import android.os.Build
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import com.saengsaengtalk.app.APIS.SectionMenu
import com.saengsaengtalk.app.databinding.LytBaedalMenuBinding
import java.text.DecimalFormat

class BaedalMenuAdapter(val menus: List<SectionMenu>) : RecyclerView.Adapter<BaedalMenuAdapter.CustomViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CustomViewHolder {
        val binding = LytBaedalMenuBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CustomViewHolder(binding)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(holder: CustomViewHolder, position: Int) {
        val menu = menus.get(position)

        holder.itemView.setOnClickListener {
            itemClickListener.onClick(menu._id)
        }
        holder.bind(menu)
    }

    interface OnItemClickListener {
        fun onClick(menuId: String)
    }

    fun setItemClickListener(onItemClickListener: OnItemClickListener) {
        this.itemClickListener = onItemClickListener
    }

    private lateinit var itemClickListener : OnItemClickListener

    override fun getItemCount(): Int {
        return menus.size
    }

    class CustomViewHolder(var binding: LytBaedalMenuBinding) : RecyclerView.ViewHolder(binding.root) {
        val dec = DecimalFormat("#,###")

        fun bind(menu: SectionMenu) {
            binding.tvName.text = menu.name
            binding.tvPrice.text = "%s원".format(dec.format(menu.price))
        }
    }
}