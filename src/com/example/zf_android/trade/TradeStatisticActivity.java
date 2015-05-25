package com.example.zf_android.trade;

import static com.example.zf_android.trade.Constants.TradeIntent.CLIENT_NUMBER;
import static com.example.zf_android.trade.Constants.TradeIntent.END_DATE;
import static com.example.zf_android.trade.Constants.TradeIntent.START_DATE;
import static com.example.zf_android.trade.Constants.TradeIntent.TRADE_TYPE;

import java.text.DecimalFormat;
import java.text.NumberFormat;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import com.epalmpay.user_phone.R;
import com.examlpe.zf_android.util.TitleMenuUtil;
import com.example.zf_android.BaseActivity;
import com.example.zf_android.trade.common.HttpCallback;
import com.example.zf_android.trade.entity.TradeStatistic;
import com.google.gson.reflect.TypeToken;

public class TradeStatisticActivity extends BaseActivity {

	private int mTradeType;
	private String mStartDate;
	private String mEndDate;
	private String mClientNumber;

	private TextView statisticAmount;
	private TextView statisticCount;
	private TextView statisticTime;
	private TextView statisticClient;
	private TextView statisticChannel;
	private TextView tradeType;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Intent intent = getIntent();
		mTradeType = intent.getIntExtra(TRADE_TYPE, 1);
		mStartDate = intent.getStringExtra(START_DATE);
		mEndDate = intent.getStringExtra(END_DATE);
		mClientNumber = intent.getStringExtra(CLIENT_NUMBER);

		setContentView(R.layout.activity_trade_statistic);
		initViews();

		API.getTradeRecordStatistic(this, mTradeType, mClientNumber,
				mStartDate, mEndDate, new HttpCallback<TradeStatistic>(this) {
					@Override
					public void onSuccess(TradeStatistic data) {
						DecimalFormat df = (DecimalFormat) NumberFormat
								.getInstance();
						df.applyPattern("0.00");
						statisticAmount.setText(getString(R.string.notation_yuan)
								+ df.format(data.getAmountTotal() * 1.0f / 100));
						statisticCount.setText("" + data.getTradeTotal());
						statisticTime.setText(mStartDate.replaceAll("-", "/")
								+ " - " + mEndDate.replaceAll("-", "/"));
						statisticClient.setText(data.getTerminalNumber());
						statisticChannel.setText(data.getPayChannelName());
						switch (data.getTradeTypeId()) {
						case 1:
							tradeType.setText("消费");
							break;
						case 2:
							tradeType.setText("转账");
							break;
						case 3:
							tradeType.setText("还款");
							break;
						case 4:
							tradeType.setText("话费充值");
							break;
						case 5:
							tradeType.setText("生活充值");
							break;
						default:
							break;
						}
					}

					@Override
					public TypeToken<TradeStatistic> getTypeToken() {
						return new TypeToken<TradeStatistic>() {
						};
					}
				});
	}

	private void initViews() {
		new TitleMenuUtil(this, getString(R.string.title_trade_statistic))
				.show();

		statisticAmount = (TextView) findViewById(R.id.trade_statistic_amount);
		statisticCount = (TextView) findViewById(R.id.trade_statistic_count);
		statisticTime = (TextView) findViewById(R.id.trade_statistic_time);
		statisticClient = (TextView) findViewById(R.id.trade_statistic_client);
		statisticChannel = (TextView) findViewById(R.id.trade_statistic_channel);
		tradeType = (TextView) findViewById(R.id.tradeType);
	}
}
