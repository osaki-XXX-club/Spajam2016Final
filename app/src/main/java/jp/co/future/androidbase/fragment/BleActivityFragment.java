package jp.co.future.androidbase.fragment;

import android.app.Activity;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import jp.co.future.androidbase.R;

/**
 * A placeholder fragment containing a simple view.
 */
public class BleActivityFragment extends Fragment {

    private OnFragmentInteractionListener mListener;

    public BleActivityFragment() {
    }

//    @Override
//    public View onCreateView(LayoutInflater inflater, ViewGroup container,
//                             Bundle savedInstanceState) {
//        return inflater.inflate(R.layout.fragment_main, container, false);
//    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_ble, container, false);
//		setTextSizeByInch(rootView);
        // BLE検索ボタンがクリックされた時の処理
        rootView.findViewById(R.id.btn_BleSearch).setOnClickListener(buttonClickListener);

    // BLE検索ストップボタンがクリックされた時の処理
        rootView.findViewById(R.id.btn_BleSearch_stop).setOnClickListener(buttonClickListener);

        return rootView;
    }

    // ボタンがクリックされた時のリスナー
    View.OnClickListener buttonClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            onButtonPressed(v);
        }
    };

    // ボタンがクリックされた時
    public void onButtonPressed(View v) {
        if (mListener != null) {
            int id = v.getId();
            if (id == R.id.btn_BleSearch) {
                mListener.onBleSearchClicked(v);
            }else if (id == R.id.btn_BleSearch_stop){
                mListener.onBleSearchStopClicked(v);
            }

        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this fragment to allow an interaction in this
     * fragment to be communicated to the activity and potentially other fragments contained in that activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html" >Communicating with Other
     * Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // BLE検索ボタンがクリックされた時
        public void onBleSearchClicked(View v);
        public void onBleSearchStopClicked(View v);


    }


}
