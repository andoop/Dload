# Dload
###一个动态加载jar包的实例
###github：https://github.com/andoop/Dload
---
##概念▪说明

 >#####动态加载：
 >此处的动态加载是指从服务端或者其他地方获取jar包，并在运行时期，加载jar包，并与         jar包互相调用。
 >本例中，为了方便演示，将要动态加载的jar放到了assets目录下，在程序运行时期，将其加载到/data/data/pkgname/files下，来模拟从服务端获取
 >#####为什么要动态加载：
 >1. 减少应用安装包体积， 程序包很大时，将部分模块打成jar包，动态的从服务端获取，然后加载
 >2. 方便升级维护，将主要功能放入动态jar包中，主应用（不包含动态jar包的部分）主要来维护动态jar，包括jar的加载，升级等。升级应用，可以更新动态jar包，用户在不重新安装的情况下，就能做到部分（强调部分，是因为它比较适用于部分功能升级）升级
 >3. 插件化开发，动态jar包可以当做插件来开发，在应用中，需要什么功能，就下载什么插件，如一些皮肤主题类的功能，可以作为插件功能来开发，用户更换皮肤或者主题时，只需要下载和更新对应的插件就行，如：桌面系统（不同的桌面主题），锁屏（不同的锁屏界面和风格）
 >4. 感觉还有好多好处，不一一列举了........
 
---
#####demo比较简单，但是能演示和介绍整个流程以及思想就行啦，干脆利索，效果图如下：

<img src="http://i.imgur.com/00hc5YT.png" width = "220" height = "350" alt="图片名称" align=center />
<img src="http://i.imgur.com/WxTxIM5.png" width = "220" height = "350" alt="图片名称" align=center />

<img src="http://i.imgur.com/U4WRLPO.png" width = "220" height = "350" alt="图片名称" align=center />

#####打开插件就会进入插件中的页面（ListFragment），点击任何一个条目，进入插件中另一个页面（DetailFragment）

##功能▪实现细节

####首先从一个类入手，这个类是功能核心，那就是ClassLoader,这个类可以让我们实现动态加载，ClassLoader是一个抽象类，实际开发过程中，我们一般是使用其具体的子类DexClassLoader、PathClassLoader这些类加载器来加载类的，它们的不同之处是：

1. DexClassLoader可以加载jar/apk/dex，可以从SD卡中加载未安装的apk；
2. PathClassLoader只能加载系统中已经安装过的apk；

####所以，因为我们要加载jar，所以我们选择DexClassLoader。

---

####看一下整个工程目录结构

![](http://i.imgur.com/cQNEhy9.png)

#####宿主工程和动态工程都依赖dloadlib，dloadlib中定义了两个工程都交互的接口，并对动态jar进行加载和调用

---

####看一下dloadlib工程结构
![](http://i.imgur.com/6JOlhrz.png)

---
####看一下代码细节

宿主工程中：

	public class MainActivity extends Activity {
	
	    @Override
	    protected void onCreate(Bundle savedInstanceState) {
	        super.onCreate(savedInstanceState);
	        setContentView(R.layout.activity_main);
			//初始化插件
	        Dload.getInstance(this).init(new DloadListener() {
				
				@Override
				public void onSuccess() {
					// TODO Auto-generated method stub
					Log.e(">>>","sccess");
				}
				
				@Override
				public void onStart() {
					// TODO Auto-generated method stub
					Log.e(">>>","start");
				}
				
				@Override
				public void onProgress(int persent) {
					// TODO Auto-generated method stub
					Log.e(">>>","progress>"+persent+"%");
				}
				
				@Override
				public void onFail(String err) {
					// TODO Auto-generated method stub
					Log.e(">>>","fail>"+err);
				}
			});
	    }
	
	  
	    public void showList(View view){
            //打开插件中ListFragment
	    	Dload.getInstance(this).showList();
	    }
	}

Dload类中逻辑

	public class Dload {
	
		private Context context;
		private DloadProxy dloadProxy;
		
		public static Dload INSTANCE;
		private Dload(Context context){
			this.context = context;
		}
		//单例
		public static Dload getInstance(Context context){
			
		  if(INSTANCE==null){
			  synchronized (Dload.class) {
				  if(INSTANCE==null){
					  INSTANCE=new Dload(context);
				  }
				
			}
		  }
		  return INSTANCE;
		}
		/**
		 * 初始化状态，例如：加载插件
		 */
		public void init(DloadListener dloadListener){
			//真正去加载插件
			DexExcutor.getInstance(context).init(dloadListener);
			//初始化代理类
			dloadProxy=new DloadProxy();
		}
		/**
		 * 打开列表，最终会调用插件功能
		 */
		public void showList(){
			//调用代理类的方法，最终会调用插件功能
			dloadProxy.showList(context);
		}
	}

看一下怎么初始化插件，DexExcutor类走起..

	public class DexExcutor {
	
		public static DexExcutor INSTANCE;
		private Context context;
		private DloadListener dloadListener;
		private String filepath;
		private DexClassLoader dexClassLoader;
		private DexExcutor(Context context){
			this.context = context;
			filepath=context.getFilesDir().getAbsolutePath();
		}
        //单例
		public static DexExcutor getInstance(Context context){
			
		  if(INSTANCE==null){
			  synchronized (Dload.class) {
				  if(INSTANCE==null){
					  INSTANCE=new DexExcutor(context);
				  }
				
			}
		  }
		  return INSTANCE;
		}
		//初始化插件
		public void init(DloadListener dloadListener){
			this.dloadListener = dloadListener;
			if(dloadListener!=null)
				dloadListener.onStart();
			
			//检查插件是否存在
			File file = new File(filepath+File.separator+MConstans.jarname);
			if(file.exists()){
				//存在，加载插件
				loadPlugin();
			}else{
				//不存在，则将插件从asset或者服务端加载到本地
				downloadPlugin();
			}
		}
		
		private void downloadPlugin() {
			// TODO Auto-generated method stub
			DloadUtils.downloadPlugin(context,"plugin.jar",filepath+File.separator+MConstans.jarname,new DloadUtils.Downloadlistener() {
				
				@Override
				public void onSuccess() {
					// TODO Auto-generated method stub
					loadPlugin();
				}
				
				@Override
				public void onStart() {
					// TODO Auto-generated method stub
					
				}
				
				@Override
				public void onProgress(int progress) {
					// TODO Auto-generated method stub
					if(dloadListener!=null){
						dloadListener.onProgress(progress);
					}
				}
				
				@Override
				public void onFail(String err) {
					// TODO Auto-generated method stub
					if(dloadListener!=null){
						dloadListener.onFail(err);
					}
				}
			});
			
		}
		/**
		 * 加载插件
		 */
		private void loadPlugin() {
			
			try {
				//根据插件的路径，实例化对应的dexclassloader
				dexClassLoader = new DexClassLoader(filepath+File.separator+MConstans.jarname, filepath,null, context.getClassLoader());
				if(dloadListener!=null){
					dloadListener.onProgress(100);
					dloadListener.onSuccess();
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				if(dloadListener!=null)
					dloadListener.onFail(e.getMessage());
			}
		}
		
		/**
		 * 根据类名获取一个实例
		 * 
		 * @param className 动态工程中类的全类名
		 *           
		 * @return Object
		 */
		public Object newInstance(String className) {
			try {
				if (null != dexClassLoader) {
					Class<?> dynamic_class = dexClassLoader.loadClass(className);
					return dynamic_class.newInstance();
				}
			} catch (ClassNotFoundException e) {
				Log.e(">>>", "newInstance:" + e.getMessage());
			} catch (InstantiationException e) {
				Log.e(">>>", "newInstance:" + e.getMessage());
			} catch (IllegalAccessException e) {
				Log.e(">>>", "newInstance:" + e.getMessage());
			}
			return null;
		}
	
		
	}

流程很简单，再看一下代理是怎样个代理，进入DloadProxy，

	public class DloadProxy implements Idload {
	  
		@Override
		public void showList(Context context) {
			//实例化动态工程中的类，这个类实现了Idload接口
			Idload newInstance = (Idload) DexExcutor.getInstance(context).newInstance("cn.andoop.android.dloadplugin.DloadImp");
			if(newInstance==null){
	
				Toast.makeText(context, "插件还没有加载好", Toast.LENGTH_SHORT).show();
				return;
			}
			//真正调用动态工程中的功能
			newInstance.showList(context);
		}
	}

有没有很简单！（因为是demo，所以逻辑越简单越好，没有做很多容错处理）

---
####看一下动态工程中怎样实现的吧

	public class DloadImp implements Idload {
	
		@Override
		public void showList(Context context) {
			// TODO Auto-generated method stub
			DLoadActivity.start(context, "cn.andoop.android.dloadplugin.ui.ListFragment", null);
		}
	
		
	}

DloadImp 是动态工程中的类，这个类就是对dloadlib中Idload的实现
DloadActivity是dloadlib中的acitvity，这个activity就是预先在宿主中注册过了activity，用它来承载动态工程中所有的页面（Fragment），这样看来，是不是动态工程中展现页面只能是Fragment?,其实不然，动态工程中其实可以有fragment，activity，service，等组件，只不过因为是动态加载进来的，说白了，最后这些类的实例最后都是反射得到的，他们的生命周期，系统可不管，所以直接使用动态工程中的fragment、activity、service等有生命周期的组件肯定是不行的，但是方法总是有地，那就是“占坑”，那就是预先在宿主中注册一些组件，如activity、service，然后让这些组件来代理动态工程中组件的生命周期，这样问题就解决了，但是也有其他问题呀，那就是动态工程中的资源怎样使用呢，能不能通过R去访问呢，这些问题留在后面解答，在我的dltest工程里寻找答案吧

---
####看一下DLoadActivity吧，看看怎样代理fragment生命周期
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
		//调用DLoadActivity加载fragment
		public static void start(Context context, String className, Bundle bundle) {
			Intent intent = new Intent(context, DLoadActivity.class);
			if (null != bundle) {
				intent.putExtras(bundle);
			}
			intent.putExtra(CLASS_NAME, className);
			context.startActivity(intent);
		}
	
	}

---
####动态工程中fragment又是怎样写的呢？

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
	

	private View getContentView() {
		rl_content = new RelativeLayout(getActivity());
		ListView listView = new ListView(getActivity());
		rl_content.addView(listView);
		listView.setAdapter(mBaseAdapter);
		listView.setOnItemClickListener(this);
		return rl_content;
	}
  	 ...后面代码就不粘贴啦

在这个demo中，可不能给fragment写布局文件了，必须进行代码布局了，其实也没啥嘛，布局文件能干的，我都能在代码中实现，不就是多写几行代码而已，但是图片资源怎样使用呢？
看看下面吧

---
####在动态工程中使用图片资源

这是动态工程中另一个fragment，在它中，使用了图片资源
	public class DetailFragment extends Fragment {
	
		@Override
		@Nullable
		public View onCreateView(LayoutInflater inflater,
				@Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
			// TODO Auto-generated method stub
			return getContentView();
		}
	
		//照样通过代码布局
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
	
			//获取资源，就这样，将图片名称传入这个工具类，就可以获取bp对象
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

ResourceUtils是一个工具类，就是读取文件中图片而已，有兴趣可以看看，也可以完善一下
代码分析到此结束，如有不懂，还是看看code吧，show you code！，但是本篇教程距离结束还早呢，还是往下看吧

---
####怎样将动态工程打包呢？

	task buildLib (type: Jar,dependsOn:'build') {
	    from ('build/intermediates/classes/release')
	    //包含资源目录
	    from ('src/main/assets/')
	    //from fileTree(dir: 'src/main',includes: ['assets/**'])
	
	}
自定义task，打包的时候也需要用到的图片放到jar中，需要将图片放入到assets文件夹下的assets下
执行buildLib即可生成动态工程对应的jar，所在目录为build/lib

---
####对生成的jar再次处理

android dalvik 不能直接加载变异.class文件，需要再次处理一下，编译成dex文件，通过dx命令即可，

格式如下

	dx --dex --output=out.jar in.jar

次工程对应的批处理文件（在builddex中）如下：

	cd E:\android_dev\sdk\sdk\build-tools\23.0.2\
	e:
	dx --dex --output=F:\projects\mprojects\DLoad\builddex\dex\plugin.jar F:\projects\mprojects\DLoad\builddex\dloadplugin.jar

需要处理的jar为dloadplugin.jar，生成的jar为plugin.jar，执行dx命令需要到对应文件夹下执行才行，如我的：E:\android_dev\sdk\sdk\build-tools\23.0.2\，不多解释了，很简单

最后，将生成的plugin.jar放入宿主工程的assets目录即可（本工程是这样做的，真正开发中，jar往往会放在服务端，宿主去检查时候需要更新插件），本工程只为演示而生，更多变化，以及完善，这都需要你结合实际开发而去应对。

---
####总结一下吧

动态加载用法流程如下：

1. 定义宿主和动态工程交互接口如：ILoad，并预先注册需要的组件
2. 宿主或着动态工程中实现接口
3. 生成动态jar
4. 通过dexclassloader加载动态jar包，动态jar包可以来源于服务端或者其他地方
5. 宿主调用动态工程中ILoad的实现，动态工程也可以调用宿主方法，宿主实现一下ILoad既可（工程中没有体现，原理比较简单，通过dloadlib，直接调用就行）

---
####最后说一下注意点

工程中值演示了加载一个动态jar，当然也可以加载多个，但是每一个动态jar对应一个dexclassloader对象
对jar的升级维护校验，工程没有体现，真正开发，这些都要考虑。

>暂时就这些吧，后期如有需求和疑问，还会再补充完善的
>####周一、二会不断更新内容，欢迎持续关注andoop，每周干货永不停！