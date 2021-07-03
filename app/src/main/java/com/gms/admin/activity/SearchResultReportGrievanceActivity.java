package com.gms.admin.activity;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.gms.admin.R;
import com.gms.admin.adapter.ReportGrievanceListAdapter;
import com.gms.admin.bean.support.ReportGrievance;
import com.gms.admin.bean.support.ReportGrievanceList;
import com.gms.admin.helper.AlertDialogHelper;
import com.gms.admin.helper.ProgressDialogHelper;
import com.gms.admin.interfaces.DialogClickListener;
import com.gms.admin.servicehelpers.ServiceHelper;
import com.gms.admin.serviceinterfaces.IServiceListener;
import com.gms.admin.utils.CommonUtils;
import com.gms.admin.utils.GMSConstants;
import com.gms.admin.utils.PreferenceStorage;
import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class SearchResultReportGrievanceActivity extends AppCompatActivity implements IServiceListener, DialogClickListener, ReportGrievanceListAdapter.OnItemClickListener{
    private static final String TAG = "AdvaSearchResAct";
    private LinearLayout userListView;
    View view;
    String className;
    String event = "";
    //    GeneralServiceListAdapter generalServiceListAdapter;
    private ServiceHelper serviceHelper;
    ArrayList<ReportGrievance> reportGrievanceArrayList = new ArrayList<>();
    int pageNumber = 0, totalCount = 0;
    protected ProgressDialogHelper progressDialogHelper;
    protected boolean isLoadingForFirstTime = true;
    Handler mHandler = new Handler();
    private SearchView mSearchView = null;
    String advSearch = "";
    ReportGrievanceList reportGrievanceList;
    private RecyclerView recyclerView;
    SwipeRefreshLayout swipeRefreshLayout;
    int listcount = 0;
    String page;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_result);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);

        Toolbar toolbar = (Toolbar) findViewById(R.id.activity_toolbar);

//        getSupportActionBar().hide();

        page = getIntent().getStringExtra("page");


        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.list_refresh);
        swipeRefreshLayout.setRefreshing(true);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {

                swipeRefreshLayout.setRefreshing(false);

            }
        });
        recyclerView = findViewById(R.id.recycler_view);
        className = this.getClass().getSimpleName();
//        serviceArrayList = new ArrayList<>();
        serviceHelper = new ServiceHelper(this);
        serviceHelper.setServiceListener(this);
        progressDialogHelper = new ProgressDialogHelper(this);

        event = PreferenceStorage.getSearchFor(this);
        if (!event.isEmpty()) {
            makeSearch(event, String.valueOf(listcount));
        }

        recyclerView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == android.view.MotionEvent.ACTION_UP) {

                    if (!recyclerView.canScrollVertically(1))
                    {

                        swipeRefreshLayout.setRefreshing(true);

                        loadmore();

                    }
                }
                return false;
            }
        });

        if (page.equalsIgnoreCase("category")) {
            toolbar.setTitle(getString(R.string.report_category_title));
        } else if (page.equalsIgnoreCase("status")) {
            toolbar.setTitle(getString(R.string.report_status_title));
        } else if (page.equalsIgnoreCase("sub_category")) {
            toolbar.setTitle(getString(R.string.report_sub_category_title));
        } else if (page.equalsIgnoreCase("location")) {
            toolbar.setTitle(getString(R.string.report_location_title));
        }

        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(getResources().getDrawable(R.drawable.ic_back));
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //What to do on back clicked
                finish();
            }
        });
    }

    private void loadmore() {
        listcount = listcount + 50;
        makeSearch(event, String.valueOf(listcount));
    }

    public void makeSearch(String event, String count) {
        if (CommonUtils.isNetworkAvailable(this)) {
            if (page.equalsIgnoreCase("category")) {
                getCategoryList(event, count);
            } else if (page.equalsIgnoreCase("status")) {
                getUsersList(event, count);
            } else if (page.equalsIgnoreCase("sub_category")) {
                getSubCategoryList(event, count);
            } else if (page.equalsIgnoreCase("location")) {
                getLocationList(event, count);
            }
        } else {
            AlertDialogHelper.showSimpleAlertDialog(this, getString(R.string.error_no_net));
        }

    }

    private void getCategoryList(String event, String count) {
        JSONObject jsonObject = new JSONObject();
        try {

            jsonObject.put(GMSConstants.SEARCH_TEXT, event);
            jsonObject.put(GMSConstants.KEY_FROM_DATE, PreferenceStorage.getFromDate(this));
            jsonObject.put(GMSConstants.KEY_TO_DATE, PreferenceStorage.getToDate(this));
            jsonObject.put(GMSConstants.KEY_CATEGORY, PreferenceStorage.getReportCategory(this));
            jsonObject.put(GMSConstants.KEY_OFFSET, count);
            jsonObject.put(GMSConstants.KEY_ROWCOUNT, "50");
            jsonObject.put(GMSConstants.DYNAMIC_DATABASE, PreferenceStorage.getDynamicDb(this));

        } catch (JSONException e) {
            e.printStackTrace();
        }
        progressDialogHelper.showProgressDialog(getString(R.string.progress_loading));
        String url = PreferenceStorage.getClientUrl(this) + GMSConstants.GET_REPORT_CATEGORY_SEARCH;
        serviceHelper.makeGetServiceCall(jsonObject.toString(), url);
    }

    private void getSubCategoryList(String event, String count) {
        JSONObject jsonObject = new JSONObject();
        try {

            jsonObject.put(GMSConstants.SEARCH_TEXT, event);
            jsonObject.put(GMSConstants.KEY_FROM_DATE, PreferenceStorage.getFromDate(this));
            jsonObject.put(GMSConstants.KEY_TO_DATE, PreferenceStorage.getToDate(this));
            jsonObject.put(GMSConstants.KEY_SUB_CATEGORY, PreferenceStorage.getReportSubCategory(this));
            jsonObject.put(GMSConstants.KEY_OFFSET, count);
            jsonObject.put(GMSConstants.KEY_ROWCOUNT, "50");
            jsonObject.put(GMSConstants.DYNAMIC_DATABASE, PreferenceStorage.getDynamicDb(this));

        } catch (JSONException e) {
            e.printStackTrace();
        }
        progressDialogHelper.showProgressDialog(getString(R.string.progress_loading));
        String url = PreferenceStorage.getClientUrl(this) + GMSConstants.GET_REPORT_SUB_CATEGORY_SEARCH;
        serviceHelper.makeGetServiceCall(jsonObject.toString(), url);
    }

    private void getLocationList(String event, String count) {
        JSONObject jsonObject = new JSONObject();
        try {

            jsonObject.put(GMSConstants.SEARCH_TEXT, event);
            jsonObject.put(GMSConstants.KEY_FROM_DATE, PreferenceStorage.getFromDate(this));
            jsonObject.put(GMSConstants.KEY_TO_DATE, PreferenceStorage.getToDate(this));
            jsonObject.put(GMSConstants.PAGUTHI, PreferenceStorage.getPaguthiID(this));
            jsonObject.put(GMSConstants.KEY_OFFSET, count);
            jsonObject.put(GMSConstants.KEY_ROWCOUNT, "50");
            jsonObject.put(GMSConstants.DYNAMIC_DATABASE, PreferenceStorage.getDynamicDb(this));

        } catch (JSONException e) {
            e.printStackTrace();
        }

        progressDialogHelper.showProgressDialog(getString(R.string.progress_loading));
        String url = PreferenceStorage.getClientUrl(this) + GMSConstants.GET_REPORT_LOCATION_SEARCH;
        serviceHelper.makeGetServiceCall(jsonObject.toString(), url);
    }

    private void getUsersList(String event, String count) {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put(GMSConstants.SEARCH_TEXT, event);
            jsonObject.put(GMSConstants.KEY_FROM_DATE, PreferenceStorage.getFromDate(this));
            jsonObject.put(GMSConstants.KEY_TO_DATE, PreferenceStorage.getToDate(this));
            jsonObject.put(GMSConstants.KEY_STATUS, PreferenceStorage.getReportStatus(this));
            jsonObject.put(GMSConstants.PAGUTHI, PreferenceStorage.getPaguthiID(this));
            jsonObject.put(GMSConstants.KEY_OFFSET, count);
            jsonObject.put(GMSConstants.KEY_ROWCOUNT, "50");
            jsonObject.put(GMSConstants.DYNAMIC_DATABASE, PreferenceStorage.getDynamicDb(this));

        } catch (JSONException e) {
            e.printStackTrace();
        }
        progressDialogHelper.showProgressDialog(getString(R.string.progress_loading));
        String url = PreferenceStorage.getClientUrl(this) + GMSConstants.GET_REPORT_STATUS_SEARCH;
        serviceHelper.makeGetServiceCall(jsonObject.toString(), url);
    }

    private boolean validateResponse(JSONObject response) {
        boolean signInSuccess = false;
        if ((response != null)) {
            try {
                String status = response.getString("status");
                String msg = response.getString(GMSConstants.PARAM_MESSAGE);
                Log.d(TAG, "status val" + status + "msg" + msg);

                if ((status != null)) {
                    if (status.equalsIgnoreCase("success")) {
                        signInSuccess = true;
                    } else {
                        signInSuccess = false;
                        Log.d(TAG, "Show error dialog");
                        swipeRefreshLayout.setRefreshing(false);
                        if (listcount == 0) {
                            swipeRefreshLayout.setVisibility(View.GONE);
                        }
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return signInSuccess;
    }

    @Override
    public void onResponse(final JSONObject response) {
        progressDialogHelper.hideProgressDialog();
        if (validateResponse(response)) {
            Gson gson = new Gson();
            reportGrievanceList = gson.fromJson(response.toString(), ReportGrievanceList.class);
            reportGrievanceArrayList.addAll(reportGrievanceList.getReportGrievanceArrayList());
            ReportGrievanceListAdapter mAdapter = new ReportGrievanceListAdapter(reportGrievanceArrayList, SearchResultReportGrievanceActivity.this);
            RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(this);
            recyclerView.setLayoutManager(mLayoutManager);
            recyclerView.setAdapter(mAdapter);
            recyclerView.scrollToPosition(listcount);
            swipeRefreshLayout.setRefreshing(false);
        }
    }

    @Override
    public void onError(final String error) {
        swipeRefreshLayout.setRefreshing(false);
        AlertDialogHelper.showSimpleAlertDialog(this, error);
    }


    @Override
    public void onAlertPositiveClicked(int tag) {

    }

    @Override
    public void onAlertNegativeClicked(int tag) {

    }

//    @Override
//    public void onItemClick(View view, int position) {
//        ReportGrievance user = null;
//        user = reportGrievanceArrayList.get(position);
//        Intent intent = new Intent(this, GrievanceDetailActivity.class);
//        intent.putExtra("grievanceObj", user.getid());
//        startActivity(intent);
//    }
    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        View v = getCurrentFocus();

        if (v != null &&
                (ev.getAction() == MotionEvent.ACTION_UP || ev.getAction() == MotionEvent.ACTION_MOVE) &&
                v instanceof EditText &&
                !v.getClass().getName().startsWith("android.webkit.")) {
            int scrcoords[] = new int[2];
            v.getLocationOnScreen(scrcoords);
            float x = ev.getRawX() + v.getLeft() - scrcoords[0];
            float y = ev.getRawY() + v.getTop() - scrcoords[1];

            if (x < v.getLeft() || x > v.getRight() || y < v.getTop() || y > v.getBottom())
                hideKeyboard(this);
        }
        return super.dispatchTouchEvent(ev);
    }

    public static void hideKeyboard(Activity activity) {
        if (activity != null && activity.getWindow() != null && activity.getWindow().getDecorView() != null) {
            InputMethodManager imm = (InputMethodManager)activity.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(activity.getWindow().getDecorView().getWindowToken(), 0);
        }
    }

    @Override
    public void onItemGrievanceClick(View view, int position) {

    }
}