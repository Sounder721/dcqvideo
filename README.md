# dcqvideo
Based on IjkPlayer and a simple UI controller
#Getting start
Clone the library to your project,and type the code into you layout file,like this:<br/>
<com.sounder.dcqvideo.widgets.DcqVideoView<br/>
        android:id="@+id/video"<br/>
        android:layout_width="match_parent"<br/>
        android:layout_height="200dp"/><br/>
In your Activity:<br/><br/>
   DcqVideoView mVideoView = (DcqVideoView) findViewById(R.id.video);<br/>
        mVideoView.setUp("your video url","title");<br/>
add it to onResume method:<br/>
        mVideoView.resume();<br/><br/>
when you need to close the activity,you had better to release  the resourse by using MediaPlayerManager.release() in the onDestroy method.

当然你也可以看我的<a href="http://blog.csdn.net/u011146263/article/details/60324391">CSDN博客</a>
#About
I'm just a beginner of Android, if you get some bugs or you have some good ideas, please leave a message.
My english is not good,i hope you can understand what i mean.
        
