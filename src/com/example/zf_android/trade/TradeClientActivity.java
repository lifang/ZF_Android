package com.example.zf_android.trade;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.examlpe.zf_android.util.TitleMenuUtil;
import com.example.zf_android.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TradeClientActivity extends ListActivity {

    public static final String CLIENT_NAME = "client_name";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trade_client);
        new TitleMenuUtil(this, getString(R.string.title_trade_client)).show();

        List<Map<String, Object>> items = new ArrayList<Map<String, Object>>();
        for (int i = 0; i < 100; i++) {
            Map<String, Object> item = new HashMap<String, Object>();
            item.put("name", System.currentTimeMillis() + "");
            items.add(item);
        }
        SimpleAdapter adapter = new SimpleAdapter(
                this, items,
                R.layout.trade_client_item,
                new String[]{"name"},
                new int[]{R.id.trade_client_name});
        setListAdapter(adapter);
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        TextView tv = (TextView) v.findViewById(R.id.trade_client_name);
        Intent intent = new Intent();
        intent.putExtra(CLIENT_NAME, tv.getText().toString());
        setResult(RESULT_OK, intent);
        finish();
    }
}
