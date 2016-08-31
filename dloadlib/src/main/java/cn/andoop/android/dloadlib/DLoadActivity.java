package cn.andoop.android.dloadlib;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.view.Window;
import android.widget.LinearLayout;

public class DLoadActivity extends FragmentActivity {

	private final static String CLASS_NAME = "classname";
	private String mClassName;
	private Bundle mBundle;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (null != getIntent()) {
			mClassName = getIntent().getStringExtra(CLASS_NAME);
			mBundle = getIntent().getExtras();
		}

		setContentView(getContentView());

	}

	private View getContentView() {
		// TODO Auto-generated method stub

		LinearLayout ll_content = new LinearLayout(this);
		ll_content.setOrientation(LinearLayout.VERTICAL);
		ll_content.setId(10000012);
		FragmentTransaction ft = this.getSupportFragmentManager()
				.beginTransaction();
		// 通过类名，反射获取到对应的类。既Fragment
		Fragment fragment = (Fragment) DexExcutor.getInstance(this)
				.newInstance(mClassName);
		if (null != fragment) {
			if (null != mBundle) {
				fragment.setArguments(mBundle);
			}
			ft.add(ll_content.getId(), fragment, mClassName);
			ft.commit();
		}

		return ll_content;
	}

	public static void start(Context context, String className, Bundle bundle) {
		Intent intent = new Intent(context, DLoadActivity.class);
		if (null != bundle) {
			intent.putExtras(bundle);
		}
		intent.putExtra(CLASS_NAME, className);
		context.startActivity(intent);
	}

}
