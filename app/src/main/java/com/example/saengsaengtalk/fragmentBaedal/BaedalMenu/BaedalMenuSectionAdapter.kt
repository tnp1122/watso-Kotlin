package com.example.saengsaengtalk.fragmentBaedal.BaedalMenu

import android.content.Context
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.saengsaengtalk.APIS.SectionMenuModel
import com.example.saengsaengtalk.databinding.LytBaedalMenuSectionBinding
import java.lang.ref.WeakReference


class BaedalMenuSectionAdapter(val context: Context, val sectionMenu: List<SectionMenuModel>) : RecyclerView.Adapter<BaedalMenuSectionAdapter.CustomViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CustomViewHolder {
        val binding = LytBaedalMenuSectionBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CustomViewHolder(binding)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(holder: CustomViewHolder, position: Int) {
        val section = sectionMenu[position]
        holder.bind(section)
    }

    interface OnItemClickListener {
        fun onClick(sectionId: Int, menuId: Int)
    }

    private var listener = WeakReference<OnItemClickListener>(null)

    fun itemClick(sectionId: Int, menuId: Int) {
        listener.get()?.onClick(sectionId, menuId)
    }

    fun addListener(listener: OnItemClickListener) {
        this.listener = WeakReference(listener)
    }

    override fun getItemCount(): Int {
        return sectionMenu.size
    }

    inner class CustomViewHolder(var binding: LytBaedalMenuSectionBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(section: SectionMenuModel) {
            binding.tvSection.text = section.section_name
            binding.lytSection.setOnClickListener{
                if (binding.rvMenuSection.visibility == View.VISIBLE) {
                    binding.rvMenuSection.visibility = View.GONE
                    binding.ivArrow.setRotation(180f)
                }
                else {
                    binding.rvMenuSection.visibility = View.VISIBLE
                    binding.ivArrow.setRotation(0f)
                }
            }

            binding.rvMenuSection.layoutManager =
                LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)

            val adapter = BaedalMenuAdapter(section.menu_list)
            binding.rvMenuSection.adapter = adapter

            adapter.setItemClickListener(object: BaedalMenuAdapter.OnItemClickListener {
                override fun onClick(menuId:Int) {
                    itemClick(section.section_id, menuId)
                }
            })
        }
    }
}