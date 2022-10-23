package com.example.saengsaengtalk.fragmentBaedal.BaedalOpt

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.saengsaengtalk.APIS.GroupOptionModel
import com.example.saengsaengtalk.databinding.LytBaedalOptAreaBinding
import java.lang.ref.WeakReference


class BaedalOptAreaAdapter(val context: Context, val groupOption: List<GroupOptionModel>) : RecyclerView.Adapter<BaedalOptAreaAdapter.CustomViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CustomViewHolder {
        val binding = LytBaedalOptAreaBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CustomViewHolder(binding)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(holder: CustomViewHolder, position: Int) {
        val group = groupOption[position]
        holder.bind(group)
    }

    interface OnItemClickListener {
        fun onClick(groupId: Int, isRadio: Boolean, optionId: Int, isChecked: Boolean)
    }

    private var listener = WeakReference<OnItemClickListener>(null)

    fun itemClick(groupId: Int, isRadio:Boolean, optionId: Int, isChecked: Boolean) {
        listener.get()?.onClick(groupId, isRadio, optionId, isChecked)
    }

    fun addListener(listener: OnItemClickListener) {
        this.listener = WeakReference(listener)
    }

    override fun getItemCount(): Int {
        return groupOption.size
    }

    inner class CustomViewHolder(var binding: LytBaedalOptAreaBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(group: GroupOptionModel) {
            if (group.max_orderable_quantity > 1) {
                binding.tvArea.text = group.group_name + " (최대 ${group.max_orderable_quantity.toString()}개)"
            } else binding.tvArea.text = group.group_name

            binding.rvMenuArea.layoutManager =
                LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)

            val adapter = BaedalOptAdapter(context, group.option_list, group.min_orderable_quantity, group.max_orderable_quantity)
            binding.rvMenuArea.adapter = adapter

            adapter.setItemClickListener(object: BaedalOptAdapter.OnItemClickListener {
                override fun onClick(isRadio:Boolean, optionId: Int, isChecked: Boolean) {
                    itemClick(group.group_id, isRadio, optionId, isChecked)
                }
            })
        }
    }
}