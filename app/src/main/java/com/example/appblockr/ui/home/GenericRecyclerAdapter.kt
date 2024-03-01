package com.example.appblockr.ui.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder

abstract class GenericRecyclerAdapter<T,D> (private val mList: ArrayList<T>?) : RecyclerView.Adapter<ViewHolder>() {

    abstract fun getLayoutId(): Int

    abstract fun onBinder(model: T, viewBinding: D, position: Int)

    override fun onCreateViewHolder(parent: ViewGroup, p1: Int): RecyclerView.ViewHolder {
        val binding = DataBindingUtil.inflate<ViewDataBinding>(LayoutInflater.from(parent.context), getLayoutId(), parent, false)
        val viewHolder = ItemViewHolder(binding)
        return viewHolder
    }

    override fun getItemCount(): Int {
        return mList?.size ?: 0
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        onBinder(mList?.get(position)!!, (holder as GenericRecyclerAdapter<*, *>.ItemViewHolder).mBinding as D,position = position)
    }


    internal inner class ItemViewHolder(binding: ViewDataBinding) : RecyclerView.ViewHolder(binding.root) {
        var mBinding: D

        init {
            mBinding = binding as D
        }
    }
}