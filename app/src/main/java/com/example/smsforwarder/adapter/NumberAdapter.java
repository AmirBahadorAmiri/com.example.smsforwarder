package com.example.smsforwarder.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smsforwarder.R;
import com.example.smsforwarder.activity.MainActivity;
import com.example.smsforwarder.model.NumberModel;
import com.example.smsforwarder.tools.roomdatabase.RoomDB;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.CompletableObserver;
import io.reactivex.rxjava3.core.SingleObserver;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class NumberAdapter extends RecyclerView.Adapter<NumberAdapter.MainHolder> {

    public List<NumberModel> numberModelArrayList = new ArrayList<>();

    public void loadNumber(Context context) {
        RoomDB.getInstance(context)
                .numberDao()
                .getAll()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SingleObserver<>() {
                    @Override
                    public void onSubscribe(@NonNull Disposable d) {
                    }

                    @SuppressLint("NotifyDataSetChanged")
                    @Override
                    public void onSuccess(@NonNull List<NumberModel> numberModels) {
                        if (numberModels.isEmpty()) {
                            ((MainActivity) context).dont_save_numbers.setVisibility(View.VISIBLE);
                        } else {
                            numberModelArrayList.clear();
                            numberModelArrayList.addAll(numberModels);
                            notifyItemInserted(numberModelArrayList.size()-1);
                        }
                    }

                    @Override
                    public void onError(@io.reactivex.rxjava3.annotations.NonNull Throwable e) {

                    }
                });
    }

    public void addNumber(Context context, NumberModel number) {

        RoomDB.getInstance(context)
                .numberDao()
                .insert(number)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new CompletableObserver() {
                    @Override
                    public void onSubscribe(@NonNull Disposable d) {
                    }

                    @SuppressLint("NotifyDataSetChanged")
                    @Override
                    public void onComplete() {
                        ((MainActivity) context).dont_save_numbers.setVisibility(View.INVISIBLE);
                        loadNumber(context);
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                    }
                });

    }

    @NonNull
    @Override
    public MainHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new MainHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_numbers, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull MainHolder holder, int position) {
        holder.fromNumber.setText(numberModelArrayList.get(position).getForwardFrom());
        holder.number_delete.setOnClickListener(v -> RoomDB.getInstance(holder.itemView.getContext())
                .numberDao()
                .delete(numberModelArrayList.get(position))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new CompletableObserver() {
                    @Override
                    public void onSubscribe(@NonNull Disposable d) {
                    }

                    @Override
                    public void onComplete() {
                        numberModelArrayList.remove(position);
                        notifyItemRemoved(position);
                        notifyItemRangeChanged(position, getItemCount());
                        Log.d("TAG", "onComplete");
                        if (numberModelArrayList.isEmpty()) {
                            ((MainActivity) holder.itemView.getContext()).dont_save_numbers.setVisibility(View.VISIBLE);
                        }
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                        Log.d("TAG", "onError: " + e.getMessage());
                    }
                }));
    }

    @Override
    public int getItemCount() {
        return numberModelArrayList.size();
    }

    public class MainHolder extends RecyclerView.ViewHolder {

        AppCompatTextView fromNumber;
        AppCompatImageView number_delete;

        public MainHolder(@NonNull View itemView) {
            super(itemView);
            fromNumber = itemView.findViewById(R.id.fromNumber);
            number_delete = itemView.findViewById(R.id.number_delete);
        }
    }
}
