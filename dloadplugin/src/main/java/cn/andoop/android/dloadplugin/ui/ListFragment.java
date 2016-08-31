package cn.andoop.android.dloadplugin.ui;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.LinearLayout.LayoutParams;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import cn.andoop.android.dloadlib.DLoadActivity;
import cn.andoop.android.dloadlib.model.ListItem;


public class ListFragment extends Fragment implements OnItemClickListener {

	private RelativeLayout rl_content;
	private MBaseAdapter mBaseAdapter;
	private List<ListItem> data;

	@Override
	@Nullable
	public View onCreateView(LayoutInflater inflater,
			@Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		initData();
		return getContentView();
	}
	
	/**
	 * 初始化数据
	 */
	private void initData() {
		data = new ArrayList<ListItem>();
		mBaseAdapter = new MBaseAdapter(getActivity(), data);
		ListItem listItem = new ListItem();
		listItem.id=1;
		listItem.text="android 周刊";
		listItem.url="http://www.androidweekly.cn";
		data.add(listItem);
		
		ListItem listItem2 = new ListItem();
		listItem2.id=2;
		listItem2.text="百度";
		listItem2.url="http://www.baidu.com";
		data.add(listItem2);
		
		ListItem listItem3 = new ListItem();
		listItem3.id=3;
		listItem3.text="网易";
		listItem3.url="http://163.com";
		data.add(listItem3);
	}

	private View getContentView() {
		rl_content = new RelativeLayout(getActivity());
		ListView listView = new ListView(getActivity());
		rl_content.addView(listView);
		listView.setAdapter(mBaseAdapter);
		listView.setOnItemClickListener(this);
		return rl_content;
	}
	
	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		ListItem listItem = data.get(position);
		Bundle bundle = new Bundle();
		bundle.putSerializable("item", listItem);
		DLoadActivity.start(getActivity(), "cn.andoop.android.dloadplugin.ui.DetailFragment", bundle);
	}
	
	public static class MBaseAdapter extends BaseAdapter{

		private Context context;
		private List<ListItem> data;

		public MBaseAdapter(Context context,List data){
			this.context=context;
			this.data=data;
		}
		
		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return data.size();
		}

		@Override
		public Object getItem(int position) {
			// TODO Auto-generated method stub
			return data.get(position);
		}

		@Override
		public long getItemId(int position) {
			// TODO Auto-generated method stub
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			// TODO Auto-generated method stub
			
			TextView tv = new TextView(context);
			tv.setText(data.get(position).text);
			tv.setPadding(20, 20, 20, 20);
			//tv.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
			tv.setGravity(Gravity.CENTER);
			return tv;
		}
	}
	
}
