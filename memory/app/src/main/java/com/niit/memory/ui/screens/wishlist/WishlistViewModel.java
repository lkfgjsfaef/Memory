package com.niit.memory.ui.screens.wishlist;

import android.app.Application;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;
import com.niit.memory.data.model.Wish;
import com.niit.memory.data.repository.WishRepository;
import com.niit.memory.util.QiniuHelper;
import com.niit.memory.util.TaskExecutor;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class WishlistViewModel extends AndroidViewModel {

    private final WishRepository repository;
    public final MutableLiveData<List<Wish>> wishes = new MutableLiveData<>();
    public final MutableLiveData<Integer> total = new MutableLiveData<>(0);
    public final MutableLiveData<Integer> completed = new MutableLiveData<>(0);
    public final MutableLiveData<Integer> pending = new MutableLiveData<>(0);
    public final MutableLiveData<Boolean> loading = new MutableLiveData<>(false);
    public final MutableLiveData<String> errorMessage = new MutableLiveData<>();

    private String currentStatus = null;
    private String currentCategory = null;
    private String currentOwner = "all";

    public WishlistViewModel(@NonNull Application application) {
        super(application);
        repository = new WishRepository(application);
    }

    public void loadWishes(String status, String category) {
        currentStatus = status;
        currentCategory = category;
        loading.postValue(true);
        TaskExecutor.execute(() -> {
            try {
                List<Wish> list = repository.getWishes(status, category);
                wishes.postValue(list != null ? list : new ArrayList<>());
                loadStats();
            } catch (Exception e) {
                loading.postValue(false);
                errorMessage.postValue(e.getMessage());
            }
        });
    }

    public void setOwnerFilter(String owner) {
        currentOwner = owner;
        // Owner filtering is client-side; re-trigger with current data without server call
        List<Wish> current = wishes.getValue();
        if (current != null) wishes.postValue(current);
    }

    public String getCurrentOwner() { return currentOwner; }

    private void loadStats() {
        try {
            List<Wish> all = repository.getWishes(null, null);
            if (all != null) {
                total.postValue(all.size());
                int comp = 0, pend = 0;
                for (Wish w : all) {
                    if ("completed".equals(w.getStatus())) comp++;
                    else pend++;
                }
                completed.postValue(comp);
                pending.postValue(pend);
            }
        } catch (Exception e) {
            Log.e("WishlistViewModel", "Error loading stats", e);
        }
        loading.postValue(false);
    }

    public void createWish(String title, String description, String category,
                           String status, String author, String startDate, String imageUrls) {
        loading.setValue(true);
        TaskExecutor.execute(() -> {
            try {
                Wish wish = new Wish();
                wish.setTitle(title);
                wish.setDescription(description);
                wish.setCategory(category);
                wish.setStatus(status);
                wish.setAuthor(author);
                wish.setStartDate(startDate);
                wish.setImageUrls(imageUrls);
                repository.createWish(wish);
                loading.postValue(false);
                loadWishes(currentStatus, currentCategory);
            } catch (Exception e) {
                loading.postValue(false);
                errorMessage.postValue(e.getMessage());
            }
        });
    }

    public void updateWish(long id, String title, String description, String category,
                           String status, String author, String startDate, String imageUrls) {
        loading.setValue(true);
        TaskExecutor.execute(() -> {
            try {
                Wish wish = new Wish();
                wish.setTitle(title);
                wish.setDescription(description);
                wish.setCategory(category);
                wish.setStatus(status);
                wish.setAuthor(author);
                wish.setStartDate(startDate);
                wish.setImageUrls(imageUrls);
                repository.updateWish(id, wish);
                loading.postValue(false);
                loadWishes(currentStatus, currentCategory);
            } catch (Exception e) {
                loading.postValue(false);
                errorMessage.postValue(e.getMessage());
            }
        });
    }

    public void updateWishStatus(long id, String status) {
        TaskExecutor.execute(() -> {
            try {
                repository.updateStatus(id, status);
                loadWishes(currentStatus, currentCategory);
            } catch (Exception e) {
                errorMessage.postValue(e.getMessage());
            }
        });
    }

    public void deleteWish(long id) {
        TaskExecutor.execute(() -> {
            try {
                repository.deleteWish(id);
                loadWishes(currentStatus, currentCategory);
            } catch (Exception e) {
                errorMessage.postValue(e.getMessage());
            }
        });
    }

    public String uploadImage(File file) throws Exception {
        return QiniuHelper.uploadImage(getApplication(), file);
    }
}
