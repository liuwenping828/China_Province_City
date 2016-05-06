package com.wenping.chinacity.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.wenping.chinacity.R;
import com.wenping.chinacity.adapter.CityAdapter;
import com.wenping.chinacity.constant.Constant;
import com.wenping.chinacity.db.DBManager;
import com.wenping.chinacity.db.OperationDB;
import com.wenping.chinacity.domain.City;
import com.wenping.chinacity.domain.Province;
import com.wenping.chinacity.utils.ImageLoader;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import jp.wasabeef.recyclerview.animators.FadeInUpAnimator;
import rx.Observable;
import rx.Observer;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func0;
import rx.schedulers.Schedulers;

public class ChoiceCityActivity extends BaseActivity {
    private static String TAG = ChoiceCityActivity.class.getSimpleName();
    private RecyclerView mRecyclerView;
    private ProgressBar mProgressBar;
    private DBManager mDBManager;
    private OperationDB mOperationDB;
    private CollapsingToolbarLayout collapsingToolbarLayout;

    private ArrayList<String> dataList = new ArrayList<>();
    private Province selectedProvince;
    private City selectedCity;
    private List<Province> provincesList;
    private List<City> cityList;
    private CityAdapter mAdapter;

    public static final int LEVEL_PROVINCE = 1;
    public static final int LEVEL_CITY = 2;
    private int currentLevel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choice_city);

        Observable.defer(new Func0<Observable<Integer>>() {
            @Override
            public Observable<Integer> call() {
                mDBManager = new DBManager(ChoiceCityActivity.this);
                mDBManager.openDatabase();
                mOperationDB = new OperationDB(ChoiceCityActivity.this);
                return Observable.just(1);
            }
        }).subscribeOn(Schedulers.io())
          .observeOn(AndroidSchedulers.mainThread())
          .subscribe(new Subscriber<Integer>() {
              @Override
              public void onCompleted() { }

              @Override
              public void onError(Throwable e) { }

              @Override
              public void onNext(Integer integer) {
                  initView();
                  initRecyclerView();
                  queryProvinces();
              }
          });
    }

    private void initView() {
        ImageView banner = (ImageView) findViewById(R.id.banner);
        collapsingToolbarLayout = (CollapsingToolbarLayout) findViewById(R.id.toolbar_layout);
        setStatusBarColor(R.color.colorSunrise);
        //彩蛋(自动昼夜状态)
        if (banner != null) {
            ImageLoader.loadAndDiskCache(this, R.mipmap.city_day, banner);
            int currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
            if (currentHour < 6 || currentHour > 18) {
                collapsingToolbarLayout.setContentScrimColor(ContextCompat.getColor(this, R.color.colorSunset));
                ImageLoader.loadAndDiskCache(this, R.mipmap.city_night, banner);
                setStatusBarColor(R.color.colorSunset);
            }
        }
        mProgressBar = (ProgressBar) findViewById(R.id.progressBar);
        if (mProgressBar != null) {
            mProgressBar.setVisibility(View.VISIBLE);
        }
    }

    private void initRecyclerView() {
        mRecyclerView = (RecyclerView) findViewById(R.id.recyclerview);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setItemAnimator(new FadeInUpAnimator());
        mAdapter = new CityAdapter(this, dataList);
        mRecyclerView.setAdapter(mAdapter);

        mAdapter.setOnItemClickListener(new CityAdapter.OnRecyclerViewItemClickListener() {
            @Override
            public void onItemClick(View view, int pos) {
                if (currentLevel == LEVEL_PROVINCE) {
                    selectedProvince = provincesList.get(pos);
                    mRecyclerView.scrollTo(0, 0);
                    queryCities();
                } else if (currentLevel == LEVEL_CITY) {
                    selectedCity = cityList.get(pos);
                    Intent intent = new Intent();
                    String cityName = selectedCity.CityName;
                    intent.putExtra(Constant.CITY_NAME, cityName);
                    setResult(Constant.resultCode, intent);
                    finish();
                }
            }
        });
    }

    /**
     * 查询全国所有的省，从数据库查询
     */
    private void queryProvinces() {
        collapsingToolbarLayout.setTitle("选择省份");
        Observer<Province> observer = new Observer<Province>() {
            @Override
            public void onCompleted() {
                currentLevel = LEVEL_PROVINCE;
                mAdapter.notifyDataSetChanged();
                mProgressBar.setVisibility(View.GONE);
//                Log.d(TAG, "省份");
            }

            @Override
            public void onError(Throwable e) { }

            @Override
            public void onNext(Province province) {
                //在这里做 RV 的动画效果 使用 Item 的更新
                dataList.add(province.ProName);
                //PLog.i(TAG,province.ProSort+"");
                //mAdapter.notifyItemInserted(province.ProSort-1);
            }
        };

        Observable.defer(new Func0<Observable<Province>>() {
            @Override
            public Observable<Province> call() {
                provincesList = mOperationDB.loadProvinces(mDBManager.getDatabase());
                return Observable.from(provincesList);
            }
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(observer);
    }

    /**
     * 查询选中省份的所有城市，从数据库查询
     */
    private void queryCities() {
        dataList.clear();
        mAdapter.notifyDataSetChanged();
        collapsingToolbarLayout.setTitle(selectedProvince.ProName);
        Observer<City> observer = new Observer<City>() {
            @Override
            public void onCompleted() {
                currentLevel = LEVEL_CITY;
                mAdapter.notifyDataSetChanged();
                //定位到第一个item
                mRecyclerView.smoothScrollToPosition(0);
                //PLog.i(TAG,"城市");
            }

            @Override
            public void onError(Throwable e) {  }

            @Override
            public void onNext(City city) {
                dataList.add(city.CityName);
                //mAdapter.notifyItemInserted(city.CitySort);
            }
        };

        Observable.defer(new Func0<Observable<City>>() {
            @Override
            public Observable<City> call() {
                cityList = mOperationDB.loadCities(mDBManager.getDatabase(), selectedProvince.ProSort);
                return Observable.from(cityList);
            }
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(observer);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
//            Log.d(TAG, "onKeyDown()--->Back");
            if (currentLevel == LEVEL_PROVINCE) {
                finish();
            } else {
                dataList.clear();
                queryProvinces();
                mRecyclerView.smoothScrollToPosition(0);
            }
        }
        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mDBManager.closeDatabase();

    }
}
