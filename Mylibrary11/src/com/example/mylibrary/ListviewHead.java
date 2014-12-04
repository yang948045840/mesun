package com.example.mylibrary;

import java.text.SimpleDateFormat;
import java.util.Date;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;



/**
 * @author Administrator
 * 
 */
public class ListviewHead extends ListView implements OnScrollListener {
	/**
	 * 整个 头布局对象linearlayout
	 */
	private LinearLayout headlinearlayout;

	/**
	 * 自定义头布局
	 */
	private View toubuju;

	private View lunbotu;

	private int downy = -1;

	private int toubujugaodu;
	/**
	 * 下拉状态
	 */
	public final int PULL_DOWN = 0;
	/**
	 * 释放刷新
	 */
	public final int SHIFANG_REFRESH = 1;
	/**
	 * 正在刷新
	 */

	public final int REFRESHING = 2;
	/**
	 * 当前头布局状态 默认是下拉刷新
	 */
	private int currentState = PULL_DOWN;

	private ImageView donghuaimage;

	private TextView shuaxin;

	private ProgressBar pb;

	private TextView shijian;

	private RotateAnimation down;

	private RotateAnimation up;
	/**
	 * listview在y轴中的值
	 */
	private int mlistviewyonscreen = -1;

	private OnRefreshListener mylistener;

	private int footerHeigth;

	private View viewfooter;
	/**
	 * 是否加载更多默认为false
	 */
	private boolean isLoadingmore = false;
	/**
	 * 默认不开启下拉刷新
	 */
	private boolean isEnabledPullDownRefresh = false;
	/**
	 * 默认不开启上啦加载
	 */
	private boolean isEnabledloadingRefresh = false;

	public ListviewHead(Context context) {
		super(context);
		initHeader();
		initFooter();
	}

	public ListviewHead(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		initHeader();
		initFooter();
	}

	public ListviewHead(Context context, AttributeSet attrs) {
		super(context, attrs);
		initHeader();
		initFooter();
	}

	/**
	 * 初始化下拉刷新头布局
	 */
	private void initHeader() {
		// 整个 头布局对象linearlayout
		headlinearlayout = (LinearLayout) View.inflate(getContext(),
				R.layout.refreshhead, null);
		// 自定义刷新头布局 progress
		toubuju = headlinearlayout.findViewById(R.id.ll_refreshhead_head);
		
		shuaxin = (TextView) headlinearlayout
				.findViewById(R.id.tv_refreshhead_shuaxin);
		pb = (ProgressBar) headlinearlayout
				.findViewById(R.id.pb_refreshhead_pb);
		shijian = (TextView) headlinearlayout
				.findViewById(R.id.tv_refreshhead_shijian);
		donghuaimage = (ImageView) headlinearlayout
				.findViewById(R.id.iv_refreshhead_xuanzhuanjiantouxia);

		// 测量刷新头的高度
		toubuju.measure(0, 0);
		toubujugaodu = toubuju.getMeasuredHeight();

		// System.out.println(toubujugaodu);
		// 隐藏头布局
		toubuju.setPadding(0, -toubujugaodu, 0, 0);
		/**
		 * 在listview添加头布局linearlayout 然后在头布局里面添加viewpager
		 */
		this.addHeaderView(headlinearlayout);

		xuanzhuandonghua();
	}

	private void xuanzhuandonghua() {

		up = new RotateAnimation(0, -180, Animation.RELATIVE_TO_SELF, 0.5f,
				Animation.RELATIVE_TO_SELF, 0.5f);
		up.setDuration(1000);

		up.setFillAfter(true);
		donghuaimage.setAnimation(up);

		down = new RotateAnimation(-180, -360, Animation.RELATIVE_TO_SELF,
				0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
		down.setDuration(1000);

		down.setFillAfter(true);
		donghuaimage.setAnimation(down);

	}

	/**
	 * 添加一个自定义的头布局 将viewpager添加到我们定义的布局当中
	 */

	public void addCustomHeaderView(View v) {
		this.lunbotu = v;
//		添加头布局
		headlinearlayout.addView(v);
	}

	@Override
	public boolean onTouchEvent(MotionEvent ev) {
		switch (ev.getAction()) {
		case MotionEvent.ACTION_DOWN:
			downy = (int) ev.getY();
			break;
		case MotionEvent.ACTION_MOVE:
			if (downy == -1) {
				downy = (int) ev.getY();
			}
			if (!isEnabledPullDownRefresh) {
				// 如果没有启用下拉刷新功能 直接跳swicth
				break;
			}
			if (currentState == REFRESHING) {// 正在刷新跳出
				break;
			}
			// 判断添加的轮播图是否完全显示了, 如果没有完全显示,
			// 不执行下面下拉头的代码, 跳转switch语句, 执行父元素的touch事件.
			// 判断轮播图是否完全显示 如果没有完全显示 就不执行下拉头的代码 跳转switch语句 执行父元素的touch事件
			// 处理回弹问题
			if (lunbotu != null) {
				int[] location = new int[2];// 0是x轴的值 1是y轴的值
				if (mlistviewyonscreen == -1) {// 没有拿过

					// 获取listview在屏幕中y轴的值
					this.getLocationOnScreen(location);
					mlistviewyonscreen = location[1];

				}
				// 获取lunbotu在屏幕的y的值
				lunbotu.getLocationOnScreen(location);
				int lunbotuy = location[1];

				if (mlistviewyonscreen > lunbotuy) {
					System.out.println("轮播图没有完全显示");
					break;
				}
			}
			
			int movey = (int) ev.getY();
			int juli = movey - downy;
			/**
			 * 如果juli差值大于0, 向下拖拽 并且 当前ListView可见的第一个条目的索引等于0 才进行下拉头的操作
			 */
			if (juli > 0 && getFirstVisiblePosition() == 0) {
				// System.out.println("juli: " + juli + "movey: " + movey
				// + "downy:  " + downy);
				int paddingtop = -toubujugaodu + juli;
				// 大于零且当前状态等于释放刷新 增加后面这个判断是为了让用户来下了了 但是继续往下拉 避免程序一直在刷新
				if (paddingtop > 0 && currentState != SHIFANG_REFRESH) {
					// 头布局完全显示
					currentState = SHIFANG_REFRESH;
					refreshPulllDownHeaderState();
				} else if (paddingtop < 0 && currentState != PULL_DOWN) {
					// 部分显示

					currentState = PULL_DOWN;
					refreshPulllDownHeaderState();
				}
				// System.out.println(paddingtop + "___=======");
				// 设置移动时的位置
				toubuju.setPadding(0, paddingtop, 0, 0);
				System.out.println("下拉头");
				return true;
			}
			break;
		case MotionEvent.ACTION_UP:
			downy = -1;
			if (currentState == PULL_DOWN) {
				// 当前妆状态是下拉刷新 把头布局隐藏
				toubuju.setPadding(0, -toubujugaodu, 0, 0);
			} else if (currentState == SHIFANG_REFRESH) {

				// 当前是释放刷新 把头布局完全显示
				toubuju.setPadding(0, 0, 0, 0);
				currentState = REFRESHING;
				refreshPulllDownHeaderState();
				// 调用用户的回调接口
				if (mylistener != null) {
					// 接口的抽象方法 子类来实现 在NewsTagDatePager页面实现
					mylistener.onPullDownRefresh();
				}
			}
			break;

		default:
			break;
		}
		return super.onTouchEvent(ev);
	}

	/**
	 * 根据当前状态来指定刷新头的布局
	 */
	private void refreshPulllDownHeaderState() {
		switch (currentState) {
		case PULL_DOWN:
			donghuaimage.startAnimation(down);
			shuaxin.setText("下拉刷新");
			break;
		case SHIFANG_REFRESH:
			donghuaimage.startAnimation(up);
			shuaxin.setText("释放刷新");
			break;
		case REFRESHING:
			donghuaimage.clearAnimation();
			donghuaimage.setVisibility(View.INVISIBLE);
			pb.setVisibility(View.VISIBLE);
			shuaxin.setText("正在刷新");
			break;

		default:
			break;
		}

	}

	/**
	 * 当数据完成时调用此方法 隐藏头布局
	 */
	public void onRefreshFinish() {

		if (isLoadingmore) {
			// 当前是加载更多的操作
			isLoadingmore = false;
			viewfooter.setPadding(0, -footerHeigth, 0, 0);
		} else {
			// 当前数下拉刷新的布局
			toubuju.setPadding(0, -toubujugaodu, 0, 0);
			currentState = PULL_DOWN;
			pb.setVisibility(View.INVISIBLE);
			shuaxin.setText("下拉刷新");
			donghuaimage.setVisibility(View.VISIBLE);
			shijian.setText("最后刷新时间" + getCurrentTime());
		}
	}

	/**
	 * 获取当前时间 格式为1990-09-09 09:09:09
	 */
	private String getCurrentTime() {
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		System.out.println(format.format(new Date()));
		return format.format(new Date());

	}

	/**
	 * 设置刷新监听事件
	 * 
	 * @param listener
	 */
	public void setOnRefreshListener(OnRefreshListener listener) {
		this.mylistener = listener;
	}

	/**
	 * 刷新回调接口
	 * 
	 * @author Administrator
	 * 
	 */
	public interface OnRefreshListener {
		/**
		 * 当下拉刷新时触发这个方法 实现此方法抓取数据
		 */
		public void onPullDownRefresh();

		public void onLoadingMroe();
	}

	private void initFooter() {
		viewfooter = View.inflate(getContext(), R.layout.blow, null);
		// 测量
		viewfooter.measure(0, 0);
		footerHeigth = viewfooter.getMeasuredHeight();
		// 隐藏
		viewfooter.setPadding(0, -footerHeigth, 0, 0);
		this.addFooterView(viewfooter);

		// 给当前的listview设置一个活动的监听
		this.setOnScrollListener(this);
	}

	/**
	 * 当滚动的状态改变时触发此方法. scrollState 当前的状态
	 * SCROLL_STATE_IDLE 停滞 SCROLL_STATE_TOUCH_SCROLL 触摸滚动 SCROLL_STATE_FLING
	 * 惯性滑动(猛的一滑)
	 */
	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {
		if (!isEnabledloadingRefresh) {
//			当前没有启动加载更多
			return;
		}
		if (scrollState == SCROLL_STATE_IDLE 
				|| scrollState == SCROLL_STATE_FLING) {
			int lastview = getLastVisiblePosition();
			if ((lastview == getCount() - 1) && !isLoadingmore) {
				// 防止在地步继续滑动而继续加载 定义变量来控制
				isLoadingmore = true;
				viewfooter.setPadding(0, 0, 0, 0);
				// 把脚布局显示出来 把listview滑动到最低边
				this.setSelection(getCount());
				if (mylistener != null) {
					mylistener.onLoadingMroe();
				}
			}
		}
	}

	/**
	 * 滚动式触发方法
	 * 
	 * @param view
	 * @param firstVisibleItem
	 * @param visibleItemCount
	 * @param totalItemCount
	 */
	@Override
	public void onScroll(AbsListView view, int firstVisibleItem,
			int visibleItemCount, int totalItemCount) {
		// TODO Auto-generated method stub

	}

	/**
	 * 是否启用下拉刷新功能
	 * @param isEnabled   真或者假
	 */
	public void isEnabledPullDownRefresh(boolean isEnabled) {
		isEnabledPullDownRefresh = isEnabled;
	}

	/**是否启用加载更多
	 * @param isloading
	 */
	public void isEnabledLoadingfresh(boolean isloading) {
		isEnabledloadingRefresh = isloading;
	}
}
