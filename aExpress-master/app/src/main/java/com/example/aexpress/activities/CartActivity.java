package com.example.aexpress.activities;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.example.aexpress.R;
import com.example.aexpress.adapters.CartAdapter;
import com.example.aexpress.databinding.ActivityCartBinding;
import com.example.aexpress.model.Product;
import com.google.android.material.snackbar.Snackbar;
import com.hishd.tinycart.model.Cart;
import com.hishd.tinycart.model.Item;
import com.hishd.tinycart.util.TinyCartHelper;

import java.util.ArrayList;
import java.util.Map;

import jp.wasabeef.recyclerview.animators.SlideInUpAnimator;

public class CartActivity extends AppCompatActivity {

    ActivityCartBinding binding;
    CartAdapter adapter;
    ArrayList<Product> products;

    private Product recentlyDeletedProduct;
    private int recentlyDeletedProductPosition;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCartBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        products = new ArrayList<>();

        Cart cart = TinyCartHelper.getCart();

        for(Map.Entry<Item, Integer> item : cart.getAllItemsWithQty().entrySet()) {
            Product product = (Product) item.getKey();
            int quantity = item.getValue();
            product.setQuantity(quantity);

            products.add(product);
        }

        adapter = new CartAdapter(this, products, new CartAdapter.CartListener() {
            @Override
            public void onQuantityChanged() {
                binding.subtotal.setText(String.format("PKR %.2f",cart.getTotalPrice()));
            }
        });

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        DividerItemDecoration itemDecoration = new DividerItemDecoration(this, layoutManager.getOrientation());
        binding.cartList.setLayoutManager(layoutManager);
        binding.cartList.addItemDecoration(itemDecoration);
        binding.cartList.setAdapter(adapter);

        binding.cartList.setItemAnimator(new SlideInUpAnimator());

        ItemTouchHelper.SimpleCallback simpleCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                // Lưu trữ thông tin về item bị xóa
                recentlyDeletedProductPosition = viewHolder.getAdapterPosition();
                recentlyDeletedProduct = products.get(recentlyDeletedProductPosition);

                // Xóa item khỏi danh sách
                adapter.removeItem(recentlyDeletedProductPosition);

                // Hiển thị Snackbar để cho phép khôi phục
                Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content), recentlyDeletedProduct.getName()+" removed", Snackbar.LENGTH_LONG);
                snackbar.setAction("UNDO", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        // Khôi phục item
                        adapter.restoreItem(recentlyDeletedProduct, recentlyDeletedProductPosition);

                        // Cập nhật lại subtotal khi item được khôi phục
                        binding.subtotal.setText(String.format("PKR %.2f", cart.getTotalPrice()));
                    }
                });
                snackbar.show();

                // Cập nhật lại subtotal
                binding.subtotal.setText(String.format("PKR %.2f", cart.getTotalPrice()));
            }
        };

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleCallback);
        itemTouchHelper.attachToRecyclerView(binding.cartList);


        binding.subtotal.setText(String.format("PKR %.2f",cart.getTotalPrice()));


        binding.continueBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(CartActivity.this, CheckoutActivity.class));
            }
        });

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return super.onSupportNavigateUp();
    }
}