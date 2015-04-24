package com.example.zf_android.activity;

import static com.example.zf_android.trade.Constants.CityIntent.CITY_ID;
import static com.example.zf_android.trade.Constants.CityIntent.CITY_NAME;
import static com.example.zf_android.trade.Constants.CityIntent.SELECTED_CITY;
import static com.example.zf_android.trade.Constants.CityIntent.SELECTED_PROVINCE;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.http.Header;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ImageView.ScaleType;

import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.location.LocationClientOption.LocationMode;
import com.examlpe.zf_android.util.ImageCacheUtil;
import com.examlpe.zf_android.util.ScreenUtils;
import com.example.zf_android.BaseActivity;
import com.example.zf_android.Config;
import com.example.zf_android.MyApplication;
import com.example.zf_android.R;
import com.example.zf_android.entity.PicEntity;
import com.example.zf_android.trade.ApplyListActivity;
import com.example.zf_android.trade.CitySelectActivity;
import com.example.zf_android.trade.TerminalManageActivity;
import com.example.zf_android.trade.TradeFlowActivity;
import com.example.zf_android.trade.entity.City;
import com.example.zf_android.trade.entity.Province;
import com.example.zf_android.trade.widget.DepthPageTransformer;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.assist.SimpleImageLoadingListener;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;

public class Main extends BaseActivity implements OnClickListener {
	private LocationClient mLocationClient;
	private TextView LocationResult;
	private RelativeLayout main_rl_pos, main_rl_renzhen, main_rl_zdgl,
			main_rl_jyls, main_rl_Forum, main_rl_wylc, main_rl_xtgg,
			main_rl_lxwm, main_rl_my, main_rl_pos1, main_rl_gwc;
	private ImageView testbutton;
	private View citySelect;
	private TextView cityTextView;
	private int cityId;
	private String cityName;

	private Province province;
	private City city;
	public static final int REQUEST_CITY = 1;
	public static final int REQUEST_CITY_WHEEL = 2;
	// vp
	private ArrayList<PicEntity> myList = new ArrayList<PicEntity>();
	private ViewPager view_pager;
	private MyAdapter adapter;
	private ImageView[] indicator_imgs;// 存放引到图片数组
	private View item;
	private LayoutInflater inflater;
	private ImageView image;
	private int index_ima = 0;
	private ArrayList<String> ma = new ArrayList<String>();
	List<View> list = new ArrayList<View>();
	private SharedPreferences mySharedPreferences;
	private Boolean islogin;
	private int id;
	private Handler handler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case 0:
				for (int i = 0; i < myList.size(); i++) {
					item = inflater.inflate(R.layout.item, null);
					list.add(item);
					ma.add(myList.get(i).getPicture_url());
				}
				indicator_imgs = new ImageView[ma.size()];
				initIndicator();
				adapter.notifyDataSetChanged();
				break;
			case 1:
				Toast.makeText(getApplicationContext(), (String) msg.obj,
						Toast.LENGTH_SHORT).show();
				break;
			case 2: // 网络有问题
				Toast.makeText(getApplicationContext(), "网络未连接",
						Toast.LENGTH_SHORT).show();
				break;
			case 3:

				break;
			case 4:
				pagerIndex++;
				pagerIndex = pagerIndex > list.size() - 1 ? 0 : pagerIndex;
				view_pager.setCurrentItem(pagerIndex);
				break;
			}
		}
	};

	private int pagerIndex = 0;
	private static final int time = 5000;
	private Timer timer = null;
	private TimerTask task = null;
	DisplayImageOptions options = new DisplayImageOptions.Builder()
			.showImageOnLoading(R.drawable.moren)
			.resetViewBeforeLoading(true)
			.cacheInMemory(false).cacheOnDisc(true)
			.imageScaleType(ImageScaleType.EXACTLY)
			.bitmapConfig(Bitmap.Config.RGB_565)
			.displayer(new FadeInBitmapDisplayer(300)).build();;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		mySharedPreferences = getSharedPreferences("Login", MODE_PRIVATE);
		islogin = mySharedPreferences.getBoolean("islogin", false);
		id = mySharedPreferences.getInt("id", 0);
		MyApplication.getInstance().setCustomerId(id);

		MyApplication.getInstance().addActivity(this);

		initView();
		testbutton = (ImageView) findViewById(R.id.testbutton);
		testbutton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent i = new Intent(Main.this, LoginActivity.class);
				startActivity(i);
			}
		});
		getdata();

		// 地图功能

		mLocationClient = ((MyApplication) getApplication()).mLocationClient;

		LocationResult = (TextView) findViewById(R.id.tv_city);
		((MyApplication) getApplication()).mLocationResult = LocationResult;
		InitLocation();
		mLocationClient.start();

		System.out.println("当前城市 ID----"
				+ MyApplication.getInstance().getCityId());

	}

	@Override
	protected void onResume() {
		super.onResume();
		mySharedPreferences = getSharedPreferences("Login", MODE_PRIVATE);
		islogin = mySharedPreferences.getBoolean("islogin", false);
		id = mySharedPreferences.getInt("id", 0);
		MyApplication.getInstance().setCustomerId(id);

		timer = new Timer();
		task = new TimerTask() {
			public void run() {
				handler.sendEmptyMessage(4);
			}
		};
		timer.schedule(task, 0, time);

	}

	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		timer.cancel();
		super.onPause();
	}

	private void InitLocation() {
		LocationClientOption option = new LocationClientOption();
		option.setLocationMode(LocationMode.Hight_Accuracy);// 设置定位模式
		option.setCoorType("gcj02");// 返回的定位结果是百度经纬度，默认值gcj02
		int span = 1000;

		option.setScanSpan(span);// 设置发起定位请求的间隔时间为5000ms
		option.setIsNeedAddress(true);
		mLocationClient.setLocOption(option);
	}

	private void getdata() {

		MyApplication
				.getInstance()
				.getClient()
				.post(Config.URL_FIGURE_GETLIST,
						new AsyncHttpResponseHandler() {

							@Override
							public void onSuccess(int statusCode,
									Header[] headers, byte[] responseBody) {
								System.out.println("-onSuccess---");
								String responseMsg = new String(responseBody)
										.toString();
								Log.e("LJP", responseMsg);

								Gson gson = new Gson();

								JSONObject jsonobject = null;
								String code = null;
								try {
									jsonobject = new JSONObject(responseMsg);
									code = jsonobject.getString("code");
									int a = jsonobject.getInt("code");
									if (a == Config.CODE) {
										String res = jsonobject
												.getString("result");
										myList = gson
												.fromJson(
														res,
														new TypeToken<List<PicEntity>>() {
														}.getType());
										handler.sendEmptyMessage(0);
									} else {
										code = jsonobject.getString("message");
										Toast.makeText(getApplicationContext(),
												code, 1000).show();
									}
								} catch (JSONException e) {
									e.printStackTrace();
								}
							}

							@Override
							public void onFailure(int statusCode,
									Header[] headers, byte[] responseBody,
									Throwable error) {
								error.printStackTrace();
							}
						});

	}

	private void initView() {

		citySelect = findViewById(R.id.titleback_linear_back);
		cityTextView = (TextView) findViewById(R.id.tv_city);
		cityTextView.setMaxWidth(ScreenUtils.getScreenWidth(this) / 5);
		citySelect.setOnClickListener(this);
		main_rl_gwc = (RelativeLayout) findViewById(R.id.main_rl_gwc);
		main_rl_gwc.setOnClickListener(this);
		main_rl_pos = (RelativeLayout) findViewById(R.id.main_rl_pos);
		main_rl_pos.setOnClickListener(this);
		main_rl_renzhen = (RelativeLayout) findViewById(R.id.main_rl_renzhen);
		main_rl_renzhen.setOnClickListener(this);
		main_rl_zdgl = (RelativeLayout) findViewById(R.id.main_rl_zdgl);
		main_rl_zdgl.setOnClickListener(this);
		main_rl_jyls = (RelativeLayout) findViewById(R.id.main_rl_jyls);
		main_rl_jyls.setOnClickListener(this);
		main_rl_Forum = (RelativeLayout) findViewById(R.id.main_rl_Forum);
		main_rl_Forum.setOnClickListener(this);
		main_rl_wylc = (RelativeLayout) findViewById(R.id.main_rl_wylc);
		main_rl_wylc.setOnClickListener(this);
		main_rl_lxwm = (RelativeLayout) findViewById(R.id.main_rl_lxwm);
		main_rl_lxwm.setOnClickListener(this);
		main_rl_xtgg = (RelativeLayout) findViewById(R.id.main_rl_xtgg);
		main_rl_xtgg.setOnClickListener(this);
		main_rl_my = (RelativeLayout) findViewById(R.id.main_rl_my);
		main_rl_my.setOnClickListener(this);
		main_rl_pos1 = (RelativeLayout) findViewById(R.id.main_rl_pos1);
		main_rl_pos1.setOnClickListener(this);

		view_pager = (ViewPager) findViewById(R.id.view_pager);
		// allow use api level>11
		// view_pager.setPageTransformer(true, new DepthPageTransformer());
		inflater = LayoutInflater.from(this);
		adapter = new MyAdapter(list);

		view_pager.setAdapter(adapter);
		// 绑定动作监听器：如翻页的动画
		view_pager.setOnPageChangeListener(new MyListener());
		// index_ima

	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {

		case R.id.titleback_linear_back:
			Intent intent = new Intent(Main.this, CitySelectActivity.class);
			cityName = cityTextView.getText().toString();
			intent.putExtra(CITY_NAME, cityName);
			startActivityForResult(intent, REQUEST_CITY);
			break;

		case R.id.main_rl_pos1: // 我的消息
			if (islogin && id != 0) {
				startActivity(new Intent(Main.this, MyMessage.class));
			} else {
				Toast.makeText(this, "请先登录", Toast.LENGTH_SHORT).show();
				startActivity(new Intent(this, LoginActivity.class));
			}

			break;

		case R.id.main_rl_my: // 我的
			if (islogin && id != 0) {
				startActivity(new Intent(Main.this, MenuMine.class));
			} else {
				Toast.makeText(this, "请先登录", Toast.LENGTH_SHORT).show();
				startActivity(new Intent(this, LoginActivity.class));
			}

			break;
		case R.id.main_rl_pos: // 买POS机

			startActivity(new Intent(Main.this, PosListActivity.class));

			break;
		case R.id.main_rl_renzhen: // 开通认证
			if (islogin && id != 0) {
				Intent i = new Intent(Main.this, ApplyListActivity.class);
				startActivity(i);
			} else {
				Toast.makeText(this, "请先登录", Toast.LENGTH_SHORT).show();
				startActivity(new Intent(this, LoginActivity.class));
			}

			break;
		case R.id.main_rl_zdgl: // 终端管理
			if (islogin && id != 0) {
				startActivity(new Intent(Main.this,
						TerminalManageActivity.class));
			} else {
				Toast.makeText(this, "请先登录", Toast.LENGTH_SHORT).show();
				startActivity(new Intent(this, LoginActivity.class));
			}
			break;
		case R.id.main_rl_jyls: // 交易流水
			if (islogin && id != 0) {
				startActivity(new Intent(Main.this, TradeFlowActivity.class));
			} else {
				Toast.makeText(this, "请先登录", Toast.LENGTH_SHORT).show();
				startActivity(new Intent(this, LoginActivity.class));
			}
			break;
		case R.id.main_rl_Forum: // 健康服务
			// startActivity(new Intent(Main.this, ChanceAdress.class));
			break;

		case R.id.main_rl_xtgg: // 系统公告

			startActivity(new Intent(Main.this, SystemMessage.class));

			break;

		case R.id.main_rl_lxwm: // 联系我们

			startActivity(new Intent(Main.this, ContentUs.class));
			break;
		case R.id.main_rl_gwc: // 购物车
			if (islogin && id != 0) {
				startActivity(new Intent(Main.this, ShopCar.class));
			} else {
				Toast.makeText(this, "请先登录", Toast.LENGTH_SHORT).show();
				startActivity(new Intent(this, LoginActivity.class));
			}
			break;
		default:
			break;
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode != RESULT_OK)
			return;
		switch (requestCode) {
		case REQUEST_CITY:
			cityId = data.getIntExtra(CITY_ID, 0);
			cityName = data.getStringExtra(CITY_NAME);
			cityTextView.setText(cityName);
			break;
		case REQUEST_CITY_WHEEL:
			province = (Province) data.getSerializableExtra(SELECTED_PROVINCE);
			city = (City) data.getSerializableExtra(SELECTED_CITY);
			cityTextView.setText(city.getName());
			break;
		}
	}

	private void initIndicator() {

		ImageView imgView;
		View v = findViewById(R.id.indicator);// 线性水平布局，负责动态调整导航图标

		for (int i = 0; i < ma.size(); i++) {
			imgView = new ImageView(this);
			LinearLayout.LayoutParams params_linear = new LinearLayout.LayoutParams(
					10, 10);
			params_linear.setMargins(7, 10, 7, 10);
			imgView.setLayoutParams(params_linear);
			indicator_imgs[i] = imgView;

			if (i == 0) { // 初始化第一个为选中状态

				indicator_imgs[i]
						.setBackgroundResource(R.drawable.indicator_focused);
			} else {
				indicator_imgs[i].setBackgroundResource(R.drawable.indicator);
			}
			((ViewGroup) v).addView(indicator_imgs[i]);
		}

	}

	/**
	 * 适配器，负责装配 、销毁 数据 和 组件 。
	 */
	private class MyAdapter extends PagerAdapter {

		private List<View> mList;
		private int index;

		public MyAdapter(List<View> list) {
			mList = list;
		}

		public int getIndex() {
			return index;
		}

		public void setIndex(int index) {
			this.index = index;
		}

		/**
		 * Return the number of views available.
		 */
		@Override
		public int getCount() {
			return mList.size();
		}

		/**
		 * Remove a page for the given position. 滑动过后就销毁 ，销毁当前页的前一个的前一个的页！
		 * instantiateItem(View container, int position) This method was
		 * deprecated in API level . Use instantiateItem(ViewGroup, int)
		 */
		@Override
		public void destroyItem(ViewGroup container, int position, Object object) {
			container.removeView(mList.get(position));

		}

		@Override
		public boolean isViewFromObject(View arg0, Object arg1) {
			return arg0 == arg1;
		}

		/**
		 * Create the page for the given position.
		 */
		@Override
		public Object instantiateItem(final ViewGroup container,
				final int position) {

			View view = mList.get(position);
			image = ((ImageView) view.findViewById(R.id.image));
			image.setScaleType(ScaleType.FIT_XY);
			// ImageCacheUtil.IMAGE_CACHE.get(ma.get(position),
			// image);

			MyApplication.getInstance().getImageLoader()
					.displayImage(ma.get(position), image, options);

			container.removeView(mList.get(position));
			container.addView(mList.get(position));
			setIndex(position);
			view.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View arg0) {
					Intent intent = new Intent();
					intent.setAction("android.intent.action.VIEW");
					Uri content_url = Uri.parse(myList.get(position)
							.getWebsite_url().toString());
					intent.setData(content_url);
					startActivity(intent);
				}
			});
			return mList.get(position);
		}
	}

	/**
	 * 动作监听器，可异步加载图片
	 * 
	 */
	private class MyListener implements OnPageChangeListener {

		@Override
		public void onPageScrollStateChanged(int state) {
			if (state == 0) {
				// new MyAdapter(null).notifyDataSetChanged();
			}
		}

		@Override
		public void onPageScrolled(int arg0, float arg1, int arg2) {

		}

		@Override
		public void onPageSelected(int position) {
			pagerIndex = position;
			// 改变所有导航的背景图片为：未选中
			for (int i = 0; i < indicator_imgs.length; i++) {
				indicator_imgs[i].setBackgroundResource(R.drawable.indicator);
			}

			// 改变当前背景图片为：选中
			index_ima = position;
			indicator_imgs[position]
					.setBackgroundResource(R.drawable.indicator_focused);
			System.out.println(index_ima + "```"
					+ myList.get(index_ima).getWebsite_url());
			View v = list.get(position);
			v.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View arg0) {

					Intent intent = new Intent();
					intent.setAction("android.intent.action.VIEW");
					Uri content_url = Uri.parse(myList.get(index_ima)
							.getWebsite_url().toString());
					intent.setData(content_url);
					startActivity(intent);
				}
			});
		}
	}
}
