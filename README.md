# WebHelper
rxjava2+retrofit
Add it in your root build.gradle at the end of repositories:

	allprojects {
		repositories {
			...
			maven { url 'https://jitpack.io' }
		}
	}
Step 2. Add the dependency

	dependencies {
	        implementation 'com.github.thankuandu:WebHelper:1.1.1'
	}



Step 3. Use

	public interface TestService {
   		@FormUrlEncoded
    		@POST("methodName")
    		Observable<ResponseBody> doSth(@Field("paramater") String paramater);
	}


	testService = RetrofitProvider.getInstance().create(TestService.class);


	testrService.doSth(result)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new MyObserver(dialog) {
                    @Override
                    protected void doSomething(String result) {
                        Log.d(TAG, result);
                    }
                });

