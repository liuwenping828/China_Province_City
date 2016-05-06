① 修改 Constant.java 中 PACKAGE_NAME,更改为自己包名
② 在应用模块 AndroidMainfext.xml 中添加
      <activity
         android:name="com.wenping.chinacity.activity.ChoiceCityActivity"
         android:theme="@style/AppTheme.NoActionBar" />
③ 在调用的 Activity 类中作如下处理
      @Override
      protected void onActivityResult(int requestCode, int resultCode, Intent data) {
          super.onActivityResult(requestCode, resultCode, data);
          if (requestCode == 设置时请求码 && resultCode == Constant.resultCode){
              // 选择城市
              data.getStringExtra(Constant.CITY_NAME);
          }
      }
④ 在应用模块 build.gradle dependencies 添加库依赖
      compile project(':chinacity')
      
参考：https://github.com/xcc3641/SeeWeather