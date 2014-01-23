package com.shouwangchong;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import com.google.ads.*;
import com.shouwangchong.CallbackBundle;
import com.shouwangchong.OpenFileDialog;
import com.shouwangchong.R;
import com.zxing.activity.CaptureActivity;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

/**
 * Tab妞ょ敻娼伴幍瀣◢濠婃垵濮╅崚鍥ㄥ床娴犮儱寮烽崝銊ф暰閺佸牊鐏� * 
 * @author D.Winter
 * @param <MyActivity>
 * 
 */
@TargetApi(Build.VERSION_CODES.GINGERBREAD_MR1)
public class InventoryChecker<MyActivity> extends Activity {
	// ViewPager閺勭棟oogle SDk娑擃叀鍤滅敮锔炬畱娑擄拷閲滈梽鍕閸栧懐娈戞稉锟介嚋缁紮绱濋崣顖欎簰閻劍娼电�鐐靛箛鐏炲繐绠烽梻瀵告畱閸掑洦宕查妴锟�
	// android-support-v4.jar
	protected static final int MENU_ABOUT = Menu.FIRST;
	protected static final int MENU_CLEAR_PREFS = Menu.FIRST+1;
	private ViewPager mPager;//椤靛崱鍐呭
	private List<View> listViews; // Tab椤甸潰鍒楄〃
	private ImageView cursor;// 鍔ㄧ敾鍥剧墖
	private TextView t1, t2, t3;// 椤靛崱澶存爣
	private int offset = 0;// 鍔ㄧ敾鍥剧墖鍋忕Щ閲�	
	private int currIndex = 0;// 褰撳墠椤靛崱缂栧彿
	private int bmpW;// 鍔ㄧ敾鍥剧墖瀹藉害
	private String inventoryFilePath;
	public static final String FILE_PATH = "INVENTORY_FILE_PATH";
	public static final String PREF = "INVENTORY_PREF";
	public static final String BARCODE_COL = "INVENTORY_BARCODE_COL";
	public static final String SCAN_DEVICE_TYPE = "INVENTORY_SCAN_DEVICE_TYPE";
	static private int openfileDialogId =0;

	private ExcelManager xls = null;
	private int searchResult;
	private String scanResult;
	private int editColIndex;
	private ArrayList<Integer> newInventory;
//	private AdView adView = null;
//	private String myAdMobId = "a150d2ba777e467";
	
	private HashMap <Integer,String> rowDefaultValue = new HashMap();
	
	private String scanDeviceType = "cam";//0 camera ,1 nfc
	
	//nfc 
	private NfcAdapter mAdapter;
    private PendingIntent mPendingIntent;
    private NdefMessage mNdefPushMessage;

    
    
	@TargetApi(Build.VERSION_CODES.GINGERBREAD_MR1)
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		//		clearPrefs(FILE_PATH);
		//		clearPrefs(BARCODE_COL);

		xls = new ExcelManager();
		newInventory = new ArrayList();
		xls.setInventoryResultTitle(InventoryChecker.this.getString(R.string.inventory_result_title));
		restorePrefs();
//		System.out.println("Main onCreate (searchColIndex:"+searchColIndex+") inventoryFilePath:"+inventoryFilePath);
		if(!xls.openFile(inventoryFilePath))
		{
			clearAllPrefs();
			showDialog(openfileDialogId);
		}
		else
		{
			iniFrontView();
		}
		
		mAdapter = NfcAdapter.getDefaultAdapter(this); // 实例化NFC设备  
		if (mAdapter == null) {  
			  
			Toast.makeText(this, "NFC is not available on this device.", Toast.LENGTH_SHORT).show();
            return;  
  
        } else if (mAdapter != null){  
            if(!mAdapter.isEnabled()) {  
            	Toast.makeText(this, "扫描NFC标签，请在系统设置中先启用NFC功能！", Toast.LENGTH_SHORT).show();
                return;  
            }else{  
                  
                Toast.makeText(this, "NFC设备就绪.", Toast.LENGTH_SHORT).show();  
                System.out.println("id:"+getNFCID(getIntent()));
                String nfcResult = getNFCID(getIntent());
                if(nfcResult.length()>0)handleResult(nfcResult);
  
            }  
        } 
	}
	
	//通过intent取NFC标签的id
	@SuppressLint("NewApi")
	private String getNFCID(Intent intent)
	{
		String ret = "";
		Parcelable[] rawMsgs = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
        NdefMessage[] msgs = null;
        if (rawMsgs != null) {
        	Parcelable tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
        	Tag p = (Tag) tag;
        	byte[] id = p.getId();
        	System.out.println("id:"+getDec(id));
            msgs = new NdefMessage[rawMsgs.length];
            ret = Long.toString(getDec(id));
            if(ret.length()>0)savePrefs(SCAN_DEVICE_TYPE,"nfc");
            }
		return ret;
	}

	
	private String getHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (int i = bytes.length - 1; i >= 0; --i) {
            int b = bytes[i] & 0xff;
            if (b < 0x10)
                sb.append('0');
            sb.append(Integer.toHexString(b));
            if (i > 0) {
                sb.append(" ");
            }
        }
        return sb.toString();
    }

	private long getDec(byte[] bytes) {
        long result = 0;  
        long factor = 1;
        int[] reMap = {3,2,1,0};//调整16进制高低位
        for (int i = 0; i < bytes.length; ++i) {
            long value = bytes[reMap[i]] & 0xffl;
            result += value * factor;
            factor *= 256l;
        }
        return result;
    }


	private void iniFrontView()
	{
		setContentView(R.layout.main);
		InitImageView();
		InitTextView();
		InitViewPager();
//		adView = new AdView(this,AdSize.BANNER,myAdMobId);
//		LinearLayout layout = (LinearLayout)findViewById(R.id.adLayout);
//		layout.addView(adView);

        // 初始化请求对象
//        adView.loadAd(new AdRequest());
		
	}

	private void restorePrefs()
	{
		SharedPreferences settings = getSharedPreferences(PREF, 0);
		try
		{
			String scan_device_type = settings.getString(this.SCAN_DEVICE_TYPE, "cam");
			if(!scan_device_type.equals(""))scanDeviceType = scan_device_type;
			System.out.println("scanDeviceType"+scanDeviceType);
			
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
//		
		String pref_file_path = settings.getString(FILE_PATH, "");
		if(! "".equals(pref_file_path))
		{
			inventoryFilePath = pref_file_path;
		}
		String pref_barcode_col = settings.getString(BARCODE_COL, "");
		if(pref_barcode_col != null)
		{
//			System.out.println(pref_barcode_col);
			try
			{
				xls.setSearchColIndex(Integer.parseInt(pref_barcode_col));
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
		}
	}


	private void savePrefs(String prefName,String prefStr)
	{
		// Save user preferences. use Editor object to make changes.
		SharedPreferences settings = getSharedPreferences(PREF, 0);
		settings.edit()
		.putString(prefName, prefStr)
		.commit();
	}

	private void clearPrefs(String prefName)
	{
		// Save user preferences. use Editor object to make changes.
		SharedPreferences settings = getSharedPreferences(PREF, 0);
		settings.edit()
		.putString(prefName, "")
		.commit();
	}
	
	private void clearAllPrefs()
	{
		clearPrefs(FILE_PATH);
		clearPrefs(BARCODE_COL);
		clearPrefs(SCAN_DEVICE_TYPE);
		inventoryFilePath = "";
		if(xls !=null)
		{
			xls.setSearchColIndex(0);
			if(xls.isFileOpened())xls.closeFile();
		}
	}

	private void handleResult(String result)
	{

		scanResult = result;
		searchResult = searchBarcode(scanResult);
		if(searchResult ==0)
		{
			//鎻愮ず鏂板

			newInventory.add(xls.getRows());
			showInventoryDetail(xls.getRows());
			Toast.makeText(InventoryChecker.this,InventoryChecker.this.getString(R.string.unknow_barcode),Toast.LENGTH_LONG).show();
		}
		else
		{
			showInventoryDetail(searchResult);
		}
	}

	private void startScan()
	{
		searchResult = 0;
		Intent openCameraIntent = new Intent(InventoryChecker.this,CaptureActivity.class);
		startActivityForResult(openCameraIntent, 0);
	}

	private void showColTitles() 
	{
		// TODO Auto-generated method stub

		setContentView(R.layout.barcode_col_selection);
		ListView mListView = (ListView) InventoryChecker.this.findViewById(R.id.lv_col_title_selection);
		List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
		Map<String, Object> map;
		ArrayList colTitle = new ArrayList();
		try
		{
			colTitle = xls.getRow(0);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		for(int i = 0; i < colTitle.size(); i++){
			map = new HashMap<String, Object>();
			map.put("info",colTitle.get(i));
			list.add(map);
		}

		mListView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> adapterView, View view, int position,
					long id) 
			{
				xls.setSearchColIndex(position);
				savePrefs(BARCODE_COL,position+"");
//				System.out.println("position:"+position);
				iniFrontView();
				startScan();
			}
		});

		SimpleAdapter adapter = new SimpleAdapter(this,list,R.layout.cell_option_list,new String[]{"info"},new int[]{R.id.tv_cell_selection_option});
		mListView.setAdapter(adapter);


	}


	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		//婢跺嫮鎮婇幍顐ｅ伎缂佹挻鐏夐敍鍫濇躬閻ｅ矂娼版稉濠冩▔锟�锟斤拷锟�		if (resultCode == RESULT_OK) {
		try
		{
			Bundle bundle = data.getExtras();
			scanResult = bundle.getString("result");
			handleResult(scanResult);
			/*
			searchResult = searchBarcode(scanResult);
			if(searchResult ==0)
			{
				//鎻愮ず鏂板

				newInventory.add(xls.getRows());
				showInventoryDetail(xls.getRows());
				Toast.makeText(InventoryChecker.this,InventoryChecker.this.getString(R.string.unknow_barcode),Toast.LENGTH_LONG).show();
			}
			else
			{
				showInventoryDetail(searchResult);
			}
			*/
		}
		catch(Exception e)
		{
			//cancel scan
			e.printStackTrace();  
		}
		//			resultTextView.setText(scanResult);
	}

	private boolean isNewInventory(int value)
	{
		try
		{
			if(newInventory.contains(value))return true; 
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		return false;
	}

	/*
	 * 鏌ユ壘鏉″舰鐮侊紝杩斿洖0涓烘病鎵惧埌璇ユ潯褰㈢爜
	 * 
	 */
	private int searchBarcode(String barCode)
	{
		int i;
//		System.out.println("searchBarcode:rows:"+xls.getRows()+" code:"+barCode);
		for(i = 1; i < xls.getRows(); i++){
//			System.out.println("i="+i+"/"+xls.getCell(i,xls.getSearchColIndex()));
			if (xls.getCell(i,xls.getSearchColIndex()).equals(barCode))return i; ;
		}
		return 0;
	}


	@SuppressLint("ShowToast")
	private void showInventoryDetail(int row)
	{
		
		boolean bNewInventory = this.isNewInventory(row);
		setContentView(R.layout.inventory_detail);
		if(xls.getCell(row, xls.getStatuesColId()).equals(InventoryChecker.this.getString(R.string.result_normal)))
		{
			Toast.makeText(InventoryChecker.this,InventoryChecker.this.getString(R.string.duplicate_check),Toast.LENGTH_SHORT).show();
		}
		else
		{
			Toast.makeText(this, "点击每一行可以自行修改内容", Toast.LENGTH_SHORT).show();
		}
		ListView mListView = (ListView) InventoryChecker.this.findViewById(R.id.lv_inventory_detail);
		List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
		Map<String, Object> map;
		String inventoryValue = null;
//		if(bNewInventory)System.out.println("SHOW NEW ");
		for(int i = 0; i < xls.getCols(); i++){
			if(bNewInventory)
			{
				searchResult = row;
			}
			inventoryValue = xls.getCell(searchResult, i);
			if(i == xls.getSearchColIndex())inventoryValue = scanResult;
			else if(inventoryValue.equals(""))
			{
				inventoryValue = getColDefaultValue(i);
			}
			map = new HashMap<String, Object>();
			map.put("title", xls.getCell(0, i));
			map.put("info", inventoryValue);
			map.put("img", R.drawable.list_file);
			list.add(map);
			inventoryValue = "";
		}
		mListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> adapterView, View view, int position,
					long id) 
			{
				if(position != xls.getSearchColIndex())
				{
					showCellSelection(searchResult,position);
				}
				else
				{
					Toast.makeText(InventoryChecker.this,InventoryChecker.this.getString(R.string.can_not_modify),Toast.LENGTH_SHORT).show();
				}
				editColIndex = position;
			}
		});

		SimpleAdapter adapter = new SimpleAdapter(this,list,R.layout.vlist,new String[]{"title","info","img"},new int[]{R.id.title,R.id.info,R.id.img});
		mListView.setAdapter(adapter);
		Button btnConfirmInventory = (Button) InventoryChecker.this.findViewById(R.id.btn_confirm_inventory);
		Button btnDenyInventory = (Button) InventoryChecker.this.findViewById(R.id.btn_deny_inventory);

		btnConfirmInventory.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				confirmInventroy(searchResult);
				startScan();
			}

		});
		btnDenyInventory.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				startScan();
			}
		});

	}

	
	private void setColDefaultValue(int col,String value)
	{
		rowDefaultValue.put(col, value);
	}
	
	private String getColDefaultValue(int col)
	{
		String ret = "";
		if(rowDefaultValue.get(col) != null)ret = rowDefaultValue.get(col);
		return ret;
	}


	private void showCellSelection(int row, int col) 
	{
		// TODO Auto-generated method stub
//				System.out.println("ShowCellSelection/row:"+row+"/col:"+col+"/searchResult:"+searchResult);
		setContentView(R.layout.cell_selection);
		ListView mListView = (ListView) InventoryChecker.this.findViewById(R.id.lv_cell_selection);
		List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
		EditText txtValueTip = (EditText) InventoryChecker.this.findViewById(R.id.cell_selection_editText);
		CheckBox chkSetDefault = (CheckBox)InventoryChecker.this.findViewById(R.id.chkSetDefault);
		String cellValue = xls.getCell(row, col);
		if(!cellValue.equals(""))
		{
			txtValueTip.setText(cellValue);
			if(getColDefaultValue(col).equals(cellValue))chkSetDefault.setChecked(true);
		}
		else if(!getColDefaultValue(col).equals(""))
		{
			txtValueTip.setText(getColDefaultValue(col));
		}
		
		Map<String, Object> map;

		for(int i = 0; i < xls.getUniqCol(col).size(); i++){
			map = new HashMap<String, Object>();
			map.put("info",xls.getUniqCol(col).get(i));
			list.add(map);
		}
		
//		chkSetDefault.setOnCheckedChangeListener(new OnCheckedChangeListener() {
//            
//            @Override
//            public void onCheckedChanged(CompoundButton arg0, boolean arg1) {
//                // TODO Auto-generated method stub
//            	
//                Toast.makeText(InventoryChecker.this, arg1?InventoryChecker.this.getString(R.string.doneDefaultSet):InventoryChecker.this.getString(R.string.cancelDefaultSet), Toast.LENGTH_LONG).show();
//            }
//
//        });

		mListView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> adapterView, View view, int position,
					long id) 
			{
				EditText txtCellSelection = (EditText) InventoryChecker.this.findViewById(R.id.cell_selection_editText);
				txtCellSelection.setText((String) xls.getUniqCol(editColIndex).get(position));
				Toast.makeText(InventoryChecker.this,InventoryChecker.this.getString(R.string.action_assign) + xls.getUniqCol(editColIndex).get(position), Toast.LENGTH_LONG).show();
			}
		});

		SimpleAdapter adapter = new SimpleAdapter(this,list,R.layout.cell_option_list,new String[]{"info"},new int[]{R.id.tv_cell_selection_option});
		mListView.setAdapter(adapter);

		Button btnCellUpdate = (Button) InventoryChecker.this.findViewById(R.id.btn_cell_update);
		btnCellUpdate.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				int intEditRow = searchResult == 0? xls.getRows():searchResult;
				EditText txtCellSelection = (EditText) InventoryChecker.this.findViewById(R.id.cell_selection_editText);
				
				CheckBox chkSetDefault = (CheckBox)InventoryChecker.this.findViewById(R.id.chkSetDefault);
				if(chkSetDefault.isChecked())
				{
					setColDefaultValue(editColIndex,txtCellSelection.getText().toString());
				}
				
				//鑷姩鏇存柊鏉＄爜鍖�				
				xls.updateCell(intEditRow, xls.getSearchColIndex(), scanResult);
				//鏇存柊鐐瑰嚮鍖哄煙
				xls.updateCell(intEditRow,editColIndex,txtCellSelection.getText().toString());
				showInventoryDetail(intEditRow);
			}

		});
	}

	private void confirmInventroy(int searchResult) {
		// TODO Auto-generated method stub
		if(searchResult >0 )
		{
			boolean bNewInventory = this.isNewInventory(searchResult);
			if(xls.getCell(searchResult, xls.getStatuesColId()).equals(InventoryChecker.this.getString(R.string.result_normal)))
			{
				Toast.makeText(InventoryChecker.this,InventoryChecker.this.getString(R.string.duplicate_check),Toast.LENGTH_SHORT);
			}
			else
			{
//				System.out.println("confirm row:"+xls.getRows()+" result:"+searchResult);
//				String resultStr = bNewInventory?InventoryChecker.this.getString(R.string.result_profit):InventoryChecker.this.getString(R.string.result_normal);
				String resultStr = xls.getCell(searchResult, xls.getStatuesColId());
//				System.out.println("resultStr:"+resultStr);
				if(!resultStr.equals(""))
				{
					//鐩樼偣鐘舵�鏍忎笉涓虹┖锛屽垯涓嶆敼鍙樿鍊�					
					resultStr = resultStr;
				}
				else if (bNewInventory)
				{
					//鏂板
					resultStr = InventoryChecker.this.getString(R.string.result_profit);
				}
				else
				{
					//姝ｅ父
					resultStr = InventoryChecker.this.getString(R.string.result_normal);
				}
				xls.updateCell(searchResult, xls.getStatuesColId(),resultStr);
			}

			iniFrontView();
		}
	}


	/**
	 * 閸掓繂顬婇崠鏍с仈閺嶏拷	 */
	private void InitTextView() {
		t1 = (TextView) findViewById(R.id.txt_page_title_1);
		t2 = (TextView) findViewById(R.id.txt_page_title_2);
		t3 = (TextView) findViewById(R.id.txt_about);

		t1.setOnClickListener(new MyOnClickListener(0));
		t2.setOnClickListener(new MyOnClickListener(1));
		t3.setOnClickListener(new MyOnClickListener(2));
	}

	/**
	 * 閸掓繂顬婇崠鏈ewPager
	 */
	private void InitViewPager() {
		mPager = (ViewPager) findViewById(R.id.vPager);
		listViews = new ArrayList<View>();
		LayoutInflater mInflater = getLayoutInflater();
//		System.out.println("mInflater:"+mInflater);
		View viewLay1 = mInflater.inflate(R.layout.lay1, null);
		View viewLay2 = mInflater.inflate(R.layout.lay1, null);
		View viewLay3 = mInflater.inflate(R.layout.lay3, null);
		listViews.add(viewLay1);
		listViews.add(viewLay2);
		listViews.add(viewLay3);
		mPager.setAdapter(new MyPagerAdapter(listViews));
		mPager.setCurrentItem(0);
		mPager.setOnPageChangeListener(new MyOnPageChangeListener());

		
		InitPageContent(viewLay1,1);
		InitPageContent(viewLay2,2);
	}
	
	private void InitPageContent(View mView,int pageId)
	{
		ListView mListView = null;
		ArrayList cat = null;
		mListView = (ListView) mView.findViewById(R.id.lv_page_1);
//		if(pageId ==2) mListView = (ListView) mView.findViewById(R.id.lv_page_2);
		List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
		Map<String, Object> map;
		if(pageId ==1)cat = xls.getCatory(xls.getStatuesColId(), "");
		if(pageId ==2)cat = xls.getCatory(xls.getStatuesColId(), InventoryChecker.this.getString(R.string.result_normal));
//		System.out.println("cat.size():"+cat.size()+ mListView);
		for(int i = 0; i < cat.size(); i++){
			try
			{
				map = new HashMap<String, Object>();
//				System.out.println("info"+"test"+i);
				ArrayList rs = (ArrayList) cat.get(i);
				String tmpStr = "";
				for(int j = 0;j<rs.size();j++)
				{
					tmpStr =tmpStr +"["+ rs.get(j)+"]";
				}
				map.put("title", rs.get(xls.getSearchColIndex()));
				map.put("info", tmpStr);
				map.put("img", R.drawable.list_file);
				list.add(map);
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
		}

		mListView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> adapterView, View view, int position,
					long id) 
			{
				startScan();
////				EditText txtCellSelection = (EditText) InventoryChecker.this.findViewById(R.id.cell_selection_editText);
////				txtCellSelection.setText((String) xls.getUniqCol(editColIndex).get(position));
////				Toast.makeText(InventoryChecker.this,InventoryChecker.this.getString(R.string.action_assign) + xls.getUniqCol(editColIndex).get(position), Toast.LENGTH_LONG).show();
			}
		});

		SimpleAdapter adapter = new SimpleAdapter(this,list,R.layout.vlist,new String[]{"title","info","img"},new int[]{R.id.title,R.id.info,R.id.img});
		mListView.setAdapter(adapter);
	}

	/**
	 * 閸掓繂顬婇崠鏍уЗ閻拷	 */
	private void InitImageView() {
		cursor = (ImageView) findViewById(R.id.cursor);
		bmpW = BitmapFactory.decodeResource(getResources(), R.drawable.a)
				.getWidth();// 閼惧嘲褰囬崶鍓у鐎硅棄瀹�		
		DisplayMetrics dm = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(dm);
		int screenW = dm.widthPixels;// 閼惧嘲褰囬崚鍡氶哺閻滃洤顔旀惔锟�	
		offset = (screenW / 3 - bmpW) / 2;// 鐠侊紕鐣婚崑蹇曅╅柌锟�	
		Matrix matrix = new Matrix();

		matrix.postTranslate(offset, 0);
//		System.out.println("cursor"+cursor+" matrix:"+matrix);
		cursor.setImageMatrix(matrix);// 鐠佸墽鐤嗛崝銊ф暰閸掓繂顬婃担宥囩枂
	}

	/**
	 * ViewPager闁倿鍘ら崳锟� */
	public class MyPagerAdapter extends PagerAdapter {
		public List<View> mListViews;

		public MyPagerAdapter(List<View> mListViews) {
			this.mListViews = mListViews;
		}

		@Override
		public void destroyItem(View arg0, int arg1, Object arg2) {
			((ViewPager) arg0).removeView(mListViews.get(arg1));
		}

		@Override
		public void finishUpdate(View arg0) {
		}

		@Override
		public int getCount() {
			return mListViews.size();
		}

		@Override
		public Object instantiateItem(View arg0, int arg1) {
			((ViewPager) arg0).addView(mListViews.get(arg1), 0);
			return mListViews.get(arg1);
		}

		@Override
		public boolean isViewFromObject(View arg0, Object arg1) {
			return arg0 == (arg1);
		}

		@Override
		public void restoreState(Parcelable arg0, ClassLoader arg1) {
		}

		@Override
		public Parcelable saveState() {
			return null;
		}

		@Override
		public void startUpdate(View arg0) {
		}
	}


	@Override
	protected Dialog onCreateDialog(int id) {
		if(id==openfileDialogId){
			Map<String, Integer> images = new HashMap<String, Integer>();
			// 涓嬮潰鍑犲彞璁剧疆鍚勬枃浠剁被鍨嬬殑鍥炬爣锛�闇�浣犲厛鎶婂浘鏍囨坊鍔犲埌璧勬簮鏂囦欢澶�			
			images.put(OpenFileDialog.sRoot, R.drawable.filedialog_root);	// 鏍圭洰褰曞浘鏍�			
			images.put(OpenFileDialog.sParent, R.drawable.filedialog_folder_up);	//杩斿洖涓婁竴灞傜殑鍥炬爣
			images.put(OpenFileDialog.sFolder, R.drawable.filedialog_folder);	//鏂囦欢澶瑰浘鏍�			
			images.put("xls", R.drawable.filedialog_xlsfile);	//涓�埇鏂囦欢鍥炬爣
			images.put(OpenFileDialog.sEmpty, R.drawable.filedialog_root);
			Dialog dialog = OpenFileDialog.createDialog(id, this, InventoryChecker.this.getString(R.string.open_file), new CallbackBundle() {
				@Override
				public void callback(Bundle bundle) {
					String filepath = bundle.getString("path");
					inventoryFilePath = filepath;
					savePrefs(FILE_PATH,filepath);
					if(!xls.openFile(filepath))
					{
						showDialog(openfileDialogId);
					}
					else if(xls.getSearchColIndex() == 0)
					{
						System.out.println("need to show Col titles");
						showColTitles();
					}
					else
					{
						iniFrontView();
					}
				}
			}, 
			".xls;",
			images);
			return dialog;
		}
		return null;
	}

	@Override
	public void onBackPressed() 
	{
		//瀹炵幇Home閿晥鏋�		//super.onBackPressed();杩欏彞璇濅竴瀹氳娉ㄦ帀,涓嶇劧鍙堝幓璋冪敤榛樿鐨刡ack澶勭悊鏂瑰紡浜�		System.out.println("back pressed");
		iniFrontView();
		new AlertDialog.Builder(this) 
		.setTitle(InventoryChecker.this.getString(R.string.yes))
		.setMessage(InventoryChecker.this.getString(R.string.quit_programe))
		.setPositiveButton(InventoryChecker.this.getString(R.string.yes),new DialogInterface.OnClickListener(){
			@Override
			public void onClick(DialogInterface arg0, int arg1) {
				// TODO Auto-generated method stub
				try
				{
					if(xls.isFileOpened())xls.closeFile();
				}
				catch(Exception e)
				{
					e.printStackTrace();
				}
				InventoryChecker.this.finish();
			}
		})
		.setNegativeButton(InventoryChecker.this.getString(R.string.no), null)
		.show();


	}

	public void onDestroy()
	{
//		adView.destroy();//还有这里退出销毁广告视图
		super.onDestroy();
		try
		{
			if(xls.isFileOpened())xls.closeFile();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public void onPause()
	{
		super.onPause();
        System.out.println("inventory checker PAUSED.");
		try
		{
			if(xls.isFileOpened())xls.closeFile();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		
	}
	
	public void onResume()
	{
		super.onResume();
        System.out.println("inventory checker RESUMED.");
        if(xls==null)xls = new ExcelManager();
		newInventory = new ArrayList();
		xls.setInventoryResultTitle(InventoryChecker.this.getString(R.string.inventory_result_title));
		restorePrefs();
		if(!xls.openFile(inventoryFilePath))
		{
			clearAllPrefs();
			showDialog(openfileDialogId);
		}
	}
	

    @Override
    public void onNewIntent(Intent intent) {
//        setIntent(intent);
        System.out.println("new intent action:"+intent.getAction());

        String nfcResult = getNFCID(intent);
        if(nfcResult.length()>0)handleResult(nfcResult);
    }
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		menu.add(0,this.MENU_ABOUT , 0, "关于...");
		menu.add(0,this.MENU_CLEAR_PREFS, 0, "重选盘点文件");
		return true;
	}
	
	public boolean onOptionsItemSelected(MenuItem item)
	{
		super.onOptionsItemSelected(item);
		switch(item.getItemId()){
		case MENU_ABOUT:
			mPager = (ViewPager) findViewById(R.id.vPager);
			mPager.setCurrentItem(2);
			break;
		case MENU_CLEAR_PREFS:
			clearAllPrefs();
			showDialog(openfileDialogId);
			break;
		}
		return true;
	}



	/**
	 * 婢跺瓨鐖ｉ悙鐟板毊閻╂垵鎯�	 */
	public class MyOnClickListener implements View.OnClickListener {
		private int index = 0;

		public MyOnClickListener(int i) {
			index = i;
		}

		@Override
		public void onClick(View v) {
			mPager.setCurrentItem(index);
			System.out.println(index);
		}
	};

	/**
	 * 妞ら潧宕遍崚鍥ㄥ床閻╂垵鎯�	 */
	public class MyOnPageChangeListener implements OnPageChangeListener {

		int one = offset * 2 + bmpW;// 妞ら潧宕� -> 妞ら潧宕� 閸嬪繒些闁诧拷		
		int two = one * 2;// 妞ら潧宕� -> 妞ら潧宕� 閸嬪繒些闁诧拷
		@Override
		public void onPageSelected(int arg0) {
			try
			{
				Animation animation = null;
				switch (arg0) {
				case 0:
					if (currIndex == 1) {
						animation = new TranslateAnimation(one, 0, 0, 0);
					} else if (currIndex == 2) {
						animation = new TranslateAnimation(two, 0, 0, 0);
					}
					break;
				case 1:
					if (currIndex == 0) {
						animation = new TranslateAnimation(offset, one, 0, 0);
					} else if (currIndex == 2) {
						animation = new TranslateAnimation(two, one, 0, 0);
					}
					break;
				case 2:
					if (currIndex == 0) {
						animation = new TranslateAnimation(offset, two, 0, 0);
					} else if (currIndex == 1) {
						animation = new TranslateAnimation(one, two, 0, 0);
					}
					break;
				}
				currIndex = arg0;
				animation.setFillAfter(true);// True:閸ュ墽澧栭崑婊冩躬閸斻劎鏁剧紒鎾存将娴ｅ秶鐤�			animation.setDuration(300);
				cursor.startAnimation(animation);
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
		}

		@Override
		public void onPageScrolled(int arg0, float arg1, int arg2) {
		}

		@Override
		public void onPageScrollStateChanged(int arg0) {
		}
	}
}