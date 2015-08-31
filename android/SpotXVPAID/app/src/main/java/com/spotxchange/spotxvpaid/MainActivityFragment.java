package com.spotxchange.spotxvpaid;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;


public class MainActivityFragment extends Fragment implements View.OnClickListener {

    private TextView _channelId;

    public MainActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main, container, false);

        Button button = (Button) view.findViewById(R.id.play_button);
        button.setOnClickListener(this);

        _channelId = (TextView) view.findViewById(R.id.channel_id);
        _channelId.setText("85394");

        return view;
    }

    @Override
    public void onClick(View v) {
        String channelId = _channelId.getText().toString();
        if (!TextUtils.isEmpty(channelId)) {
            Intent intent = new Intent(getActivity(), VpaidActivity.class)
                    .putExtra(VpaidActivity.EXTRA_CHANNEL_ID, channelId)
                    .putExtra(VpaidActivity.EXTRA_APP_DOMAIN, "com.spotxchange.vpaid");
            startActivity(intent);
        }
    }
}
