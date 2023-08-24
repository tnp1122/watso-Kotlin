package com.watso.app.feature.baedal.ui.view.baedalMenu

import android.content.Context
import android.os.Build
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.watso.app.databinding.LytBaedalMenuSectionBinding
import com.watso.app.feature.baedal.data.Section

class BaedalMenuSectionAdapter(val context: Context) : RecyclerView.Adapter<BaedalMenuSectionAdapter.CustomViewHolder>() {

    private var sections = mutableListOf<Section>()

    fun setData(sectionData: List<Section>) {
        sections.clear()
        sections.addAll(sectionData)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CustomViewHolder {
        val binding = LytBaedalMenuSectionBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CustomViewHolder(binding)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(holder: CustomViewHolder, position: Int) {
        val section = sections[position]
        holder.bind(section)
    }

    interface OnSecMenuClickListener { fun onClick(sectionName: String, menuId: String) }

    fun setSecMenuClickListener(onSecMenuClickListener: OnSecMenuClickListener) { this.secMenuClickListener = onSecMenuClickListener }

    private lateinit var secMenuClickListener: OnSecMenuClickListener

    override fun getItemCount(): Int {
        return sections.size
    }

    inner class CustomViewHolder(var binding: LytBaedalMenuSectionBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(section: Section) {
            binding.tvSection.text = section.name
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

            val adapter = BaedalMenuAdapter()
            binding.rvMenuSection.adapter = adapter
            adapter.setData(section.menus)

            adapter.setMenuClickListener(object: BaedalMenuAdapter.OnMenuClickListener {
                override fun onMenuClick(menuId: String) {
                    Log.d("섹션 어댑터", "메뉴 클릭 리스너")
                    secMenuClickListener.onClick(section.name, menuId)
                }
            })
        }
    }
}