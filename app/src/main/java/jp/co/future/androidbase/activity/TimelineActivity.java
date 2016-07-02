package jp.co.future.androidbase.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import jp.co.future.androidbase.Orientation;
import jp.co.future.androidbase.R;
import jp.co.future.androidbase.adapter.TimeLineAdapter;
import jp.co.future.androidbase.model.TimeLineModel;

public class TimelineActivity extends AppCompatActivity {

    /**
     * ログ出力用タグ
     */
    private static final String TAG = TimelineActivity.class.getSimpleName();


    private RecyclerView mRecyclerView;

    private static TimeLineAdapter mTimeLineAdapter;

    private static List<TimeLineModel> mDataList = new ArrayList<>();

    private Orientation mOrientation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timeline);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mOrientation = (Orientation) getIntent().getSerializableExtra(MainActivity.TAG_ORIENTATION);

        if (mOrientation == Orientation.horizontal) {
            setTitle("Horizontal TimeLine");
        } else {
            setTitle("Vertical TimeLine");
        }

        mRecyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        mRecyclerView.setLayoutManager(getLinearLayoutManager());
        mRecyclerView.setHasFixedSize(true);


        initView();
    }

    private LinearLayoutManager getLinearLayoutManager() {

        if (mOrientation == Orientation.horizontal) {

            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
            return linearLayoutManager;
        } else {

            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
            return linearLayoutManager;
        }

    }

    private void initView() {

//        for (int i = 0; i < 20; i++) {
//            TimeLineModel model = new TimeLineModel();
//            model.setName("Random" + i);
//            model.setAge(i);
//            mDataList.add(model);
//        }

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("message");


        mTimeLineAdapter = new TimeLineAdapter(mDataList, mOrientation);
        mRecyclerView.setAdapter(mTimeLineAdapter);

        // Read from the database
        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                String value = dataSnapshot.getValue(String.class);
                Log.d(TAG, "Value is: " + value);
                TimeLineModel model = new TimeLineModel();
                model.setName(value);
                model.setAge(100);
                mDataList.add(model);
                mTimeLineAdapter.notifyDataSetChanged();

                // 音声変換呼び出し
                Intent intent = new Intent(getApplicationContext(), TtlActivity.class);
                intent.putExtra("word", "a");
                startActivity(intent);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w(TAG, "Failed to read value.", error.toException());
            }
        });

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //Menu
        switch (item.getItemId()) {
            //When home is clicked
            case android.R.id.home:
                onBackPressed();
                return true;

            default:
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onSaveInstanceState(Bundle savedInstanceState) {

        if (mOrientation != null)
            savedInstanceState.putSerializable(MainActivity.TAG_ORIENTATION, mOrientation);

        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {

        if (savedInstanceState != null) {
            if (savedInstanceState.containsKey(MainActivity.TAG_ORIENTATION)) {
                mOrientation = (Orientation) savedInstanceState.getSerializable(MainActivity.TAG_ORIENTATION);
            }
        }

        super.onRestoreInstanceState(savedInstanceState);
    }

    public static TimeLineAdapter getmTimeLineAdapter() {
        return mTimeLineAdapter;
    }

    public static List<TimeLineModel> getmDataList() {
        return mDataList;
    }

}

