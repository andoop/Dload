package cn.andoop.android.dloadplugin.ui;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;

import cn.andoop.android.dloadlib.model.ListItem;
import cn.andoop.android.dloadlib.utils.ResourceUtils;

public class DetailFragment extends Fragment {

	@Override
	@Nullable
	public View onCreateView(LayoutInflater inflater,
			@Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		return getContentView();
	}

	private View getContentView() {
		LinearLayout linearLayout = new LinearLayout(getActivity());
		linearLayout.setOrientation(LinearLayout.VERTICAL);
		TextView title = new TextView(getActivity());
		ImageView imageView=new ImageView(getActivity());
		WebView webView = new WebView(getActivity());
		
		title.setPadding(20, 20, 20, 20);
		title.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
		title.setGravity(Gravity.CENTER);
		
		webView.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
		webView.setWebViewClient(new WebViewClient(){
			@Override
			public boolean shouldOverrideUrlLoading(WebView view, String url) {
				// TODO Auto-generated method stub
				return false;
			}
		});
		webView.getSettings().setJavaScriptEnabled(true);
		
		linearLayout.addView(title);
		linearLayout.addView(imageView);
		linearLayout.addView(webView);

		//获取资源
		Bitmap imageResouce = new ResourceUtils(getActivity()).getImageResouce("assets/car.jpg");
		imageView.setImageBitmap(imageResouce);

		Bundle bundle = getArguments();
		if(bundle!=null&&( bundle.getSerializable("item")!=null)){
			if(bundle.getSerializable("item") instanceof ListItem) {
				ListItem listitem = (ListItem) bundle.getSerializable("item");
				title.setText(listitem.text);
				webView.loadUrl(listitem.url);
			}
		}
		
		return linearLayout;
	}
}
