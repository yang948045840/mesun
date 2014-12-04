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
	 * ���� ͷ���ֶ���linearlayout
	 */
	private LinearLayout headlinearlayout;

	/**
	 * �Զ���ͷ����
	 */
	private View toubuju;

	private View lunbotu;

	private int downy = -1;

	private int toubujugaodu;
	/**
	 * ����״̬
	 */
	public final int PULL_DOWN = 0;
	/**
	 * �ͷ�ˢ��
	 */
	public final int SHIFANG_REFRESH = 1;
	/**
	 * ����ˢ��
	 */

	public final int REFRESHING = 2;
	/**
	 * ��ǰͷ����״̬ Ĭ��������ˢ��
	 */
	private int currentState = PULL_DOWN;

	private ImageView donghuaimage;

	private TextView shuaxin;

	private ProgressBar pb;

	private TextView shijian;

	private RotateAnimation down;

	private RotateAnimation up;
	/**
	 * listview��y���е�ֵ
	 */
	private int mlistviewyonscreen = -1;

	private OnRefreshListener mylistener;

	private int footerHeigth;

	private View viewfooter;
	/**
	 * �Ƿ���ظ���Ĭ��Ϊfalse
	 */
	private boolean isLoadingmore = false;
	/**
	 * Ĭ�ϲ���������ˢ��
	 */
	private boolean isEnabledPullDownRefresh = false;
	/**
	 * Ĭ�ϲ�������������
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
	 * ��ʼ������ˢ��ͷ����
	 */
	private void initHeader() {
		// ���� ͷ���ֶ���linearlayout
		headlinearlayout = (LinearLayout) View.inflate(getContext(),
				R.layout.refreshhead, null);
		// �Զ���ˢ��ͷ���� progress
		toubuju = headlinearlayout.findViewById(R.id.ll_refreshhead_head);
		
		shuaxin = (TextView) headlinearlayout
				.findViewById(R.id.tv_refreshhead_shuaxin);
		pb = (ProgressBar) headlinearlayout
				.findViewById(R.id.pb_refreshhead_pb);
		shijian = (TextView) headlinearlayout
				.findViewById(R.id.tv_refreshhead_shijian);
		donghuaimage = (ImageView) headlinearlayout
				.findViewById(R.id.iv_refreshhead_xuanzhuanjiantouxia);

		// ����ˢ��ͷ�ĸ߶�
		toubuju.measure(0, 0);
		toubujugaodu = toubuju.getMeasuredHeight();

		// System.out.println(toubujugaodu);
		// ����ͷ����
		toubuju.setPadding(0, -toubujugaodu, 0, 0);
		/**
		 * ��listview���ͷ����linearlayout Ȼ����ͷ�����������viewpager
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
	 * ���һ���Զ����ͷ���� ��viewpager��ӵ����Ƕ���Ĳ��ֵ���
	 */

	public void addCustomHeaderView(View v) {
		this.lunbotu = v;
//		���ͷ����
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
				// ���û����������ˢ�¹��� ֱ����swicth
				break;
			}
			if (currentState == REFRESHING) {// ����ˢ������
				break;
			}
			// �ж���ӵ��ֲ�ͼ�Ƿ���ȫ��ʾ��, ���û����ȫ��ʾ,
			// ��ִ����������ͷ�Ĵ���, ��תswitch���, ִ�и�Ԫ�ص�touch�¼�.
			// �ж��ֲ�ͼ�Ƿ���ȫ��ʾ ���û����ȫ��ʾ �Ͳ�ִ������ͷ�Ĵ��� ��תswitch��� ִ�и�Ԫ�ص�touch�¼�
			// ����ص�����
			if (lunbotu != null) {
				int[] location = new int[2];// 0��x���ֵ 1��y���ֵ
				if (mlistviewyonscreen == -1) {// û���ù�

					// ��ȡlistview����Ļ��y���ֵ
					this.getLocationOnScreen(location);
					mlistviewyonscreen = location[1];

				}
				// ��ȡlunbotu����Ļ��y��ֵ
				lunbotu.getLocationOnScreen(location);
				int lunbotuy = location[1];

				if (mlistviewyonscreen > lunbotuy) {
					System.out.println("�ֲ�ͼû����ȫ��ʾ");
					break;
				}
			}
			
			int movey = (int) ev.getY();
			int juli = movey - downy;
			/**
			 * ���juli��ֵ����0, ������ק ���� ��ǰListView�ɼ��ĵ�һ����Ŀ����������0 �Ž�������ͷ�Ĳ���
			 */
			if (juli > 0 && getFirstVisiblePosition() == 0) {
				// System.out.println("juli: " + juli + "movey: " + movey
				// + "downy:  " + downy);
				int paddingtop = -toubujugaodu + juli;
				// �������ҵ�ǰ״̬�����ͷ�ˢ�� ���Ӻ�������ж���Ϊ�����û��������� ���Ǽ��������� �������һֱ��ˢ��
				if (paddingtop > 0 && currentState != SHIFANG_REFRESH) {
					// ͷ������ȫ��ʾ
					currentState = SHIFANG_REFRESH;
					refreshPulllDownHeaderState();
				} else if (paddingtop < 0 && currentState != PULL_DOWN) {
					// ������ʾ

					currentState = PULL_DOWN;
					refreshPulllDownHeaderState();
				}
				// System.out.println(paddingtop + "___=======");
				// �����ƶ�ʱ��λ��
				toubuju.setPadding(0, paddingtop, 0, 0);
				System.out.println("����ͷ");
				return true;
			}
			break;
		case MotionEvent.ACTION_UP:
			downy = -1;
			if (currentState == PULL_DOWN) {
				// ��ǰױ״̬������ˢ�� ��ͷ��������
				toubuju.setPadding(0, -toubujugaodu, 0, 0);
			} else if (currentState == SHIFANG_REFRESH) {

				// ��ǰ���ͷ�ˢ�� ��ͷ������ȫ��ʾ
				toubuju.setPadding(0, 0, 0, 0);
				currentState = REFRESHING;
				refreshPulllDownHeaderState();
				// �����û��Ļص��ӿ�
				if (mylistener != null) {
					// �ӿڵĳ��󷽷� ������ʵ�� ��NewsTagDatePagerҳ��ʵ��
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
	 * ���ݵ�ǰ״̬��ָ��ˢ��ͷ�Ĳ���
	 */
	private void refreshPulllDownHeaderState() {
		switch (currentState) {
		case PULL_DOWN:
			donghuaimage.startAnimation(down);
			shuaxin.setText("����ˢ��");
			break;
		case SHIFANG_REFRESH:
			donghuaimage.startAnimation(up);
			shuaxin.setText("�ͷ�ˢ��");
			break;
		case REFRESHING:
			donghuaimage.clearAnimation();
			donghuaimage.setVisibility(View.INVISIBLE);
			pb.setVisibility(View.VISIBLE);
			shuaxin.setText("����ˢ��");
			break;

		default:
			break;
		}

	}

	/**
	 * ���������ʱ���ô˷��� ����ͷ����
	 */
	public void onRefreshFinish() {

		if (isLoadingmore) {
			// ��ǰ�Ǽ��ظ���Ĳ���
			isLoadingmore = false;
			viewfooter.setPadding(0, -footerHeigth, 0, 0);
		} else {
			// ��ǰ������ˢ�µĲ���
			toubuju.setPadding(0, -toubujugaodu, 0, 0);
			currentState = PULL_DOWN;
			pb.setVisibility(View.INVISIBLE);
			shuaxin.setText("����ˢ��");
			donghuaimage.setVisibility(View.VISIBLE);
			shijian.setText("���ˢ��ʱ��" + getCurrentTime());
		}
	}

	/**
	 * ��ȡ��ǰʱ�� ��ʽΪ1990-09-09 09:09:09
	 */
	private String getCurrentTime() {
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		System.out.println(format.format(new Date()));
		return format.format(new Date());

	}

	/**
	 * ����ˢ�¼����¼�
	 * 
	 * @param listener
	 */
	public void setOnRefreshListener(OnRefreshListener listener) {
		this.mylistener = listener;
	}

	/**
	 * ˢ�»ص��ӿ�
	 * 
	 * @author Administrator
	 * 
	 */
	public interface OnRefreshListener {
		/**
		 * ������ˢ��ʱ����������� ʵ�ִ˷���ץȡ����
		 */
		public void onPullDownRefresh();

		public void onLoadingMroe();
	}

	private void initFooter() {
		viewfooter = View.inflate(getContext(), R.layout.blow, null);
		// ����
		viewfooter.measure(0, 0);
		footerHeigth = viewfooter.getMeasuredHeight();
		// ����
		viewfooter.setPadding(0, -footerHeigth, 0, 0);
		this.addFooterView(viewfooter);

		// ����ǰ��listview����һ����ļ���
		this.setOnScrollListener(this);
	}

	/**
	 * ��������״̬�ı�ʱ�����˷���. scrollState ��ǰ��״̬
	 * SCROLL_STATE_IDLE ͣ�� SCROLL_STATE_TOUCH_SCROLL �������� SCROLL_STATE_FLING
	 * ���Ի���(�͵�һ��)
	 */
	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {
		if (!isEnabledloadingRefresh) {
//			��ǰû���������ظ���
			return;
		}
		if (scrollState == SCROLL_STATE_IDLE 
				|| scrollState == SCROLL_STATE_FLING) {
			int lastview = getLastVisiblePosition();
			if ((lastview == getCount() - 1) && !isLoadingmore) {
				// ��ֹ�ڵز������������������� �������������
				isLoadingmore = true;
				viewfooter.setPadding(0, 0, 0, 0);
				// �ѽŲ�����ʾ���� ��listview��������ͱ�
				this.setSelection(getCount());
				if (mylistener != null) {
					mylistener.onLoadingMroe();
				}
			}
		}
	}

	/**
	 * ����ʽ��������
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
	 * �Ƿ���������ˢ�¹���
	 * @param isEnabled   ����߼�
	 */
	public void isEnabledPullDownRefresh(boolean isEnabled) {
		isEnabledPullDownRefresh = isEnabled;
	}

	/**�Ƿ����ü��ظ���
	 * @param isloading
	 */
	public void isEnabledLoadingfresh(boolean isloading) {
		isEnabledloadingRefresh = isloading;
	}
}
