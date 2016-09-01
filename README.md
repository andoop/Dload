# Dload
###一个动态加载jar包的实例
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

<img src="http://i.imgur.com/00hc5YT.png" width = "300" height = "500" alt="图片名称" align=center />
<img src="http://i.imgur.com/WxTxIM5.png" width = "300" height = "500" alt="图片名称" align=center />

<img src="http://i.imgur.com/U4WRLPO.png" width = "300" height = "500" alt="图片名称" align=center />

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