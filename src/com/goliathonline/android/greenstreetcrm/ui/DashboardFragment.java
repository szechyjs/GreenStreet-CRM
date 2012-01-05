package com.goliathonline.android.greenstreetcrm.ui;

import com.goliathonline.android.greenstreetcrm.R;
import com.goliathonline.android.greenstreetcrm.provider.CustomerContract;
import com.goliathonline.android.greenstreetcrm.ui.tablet.CustomersMultiPaneActivity;
import com.goliathonline.android.greenstreetcrm.ui.tablet.JobsMultiPaneActivity;
import com.goliathonline.android.greenstreetcrm.util.UIUtils;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class DashboardFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_dashboard, container);

        // Attach event handlers
        root.findViewById(R.id.home_btn_schedule).setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
//                if (UIUtils.isHoneycombTablet(getActivity())) {
//                    startActivity(new Intent(getActivity(), BaseMultiPaneActivity.class));
//                } else {
//                    startActivity(new Intent(getActivity(), BaseSinglePaneActivity.class));
//                }
            }
        });

        root.findViewById(R.id.home_btn_sessions).setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                // Launch sessions list
                if (UIUtils.isHoneycombTablet(getActivity())) {
                    startActivity(new Intent(getActivity(), JobsMultiPaneActivity.class));
                } else {
                	final Intent intent = new Intent(Intent.ACTION_VIEW,
                            CustomerContract.Jobs.CONTENT_URI);
                    startActivity(intent);
                }
            }
        });

        root.findViewById(R.id.home_btn_starred).setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                // Launch list of sessions and vendors the user has starred
//                startActivity(new Intent(getActivity(), BaseSinglePaneActivity.class));                
            }
        });

        root.findViewById(R.id.home_btn_vendors).setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                // Launch vendors list
            	if (UIUtils.isHoneycombTablet(getActivity())) {
                    startActivity(new Intent(getActivity(), CustomersMultiPaneActivity.class));
                } else {
                    final Intent intent = new Intent(Intent.ACTION_VIEW,
                            CustomerContract.Customers.CONTENT_URI);
                    startActivity(intent);
                }
            }
        });

        root.findViewById(R.id.home_btn_map).setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                // Launch map of conference venue
//            	if (UIUtils.isHoneycombTablet(getActivity())) {
//                    startActivity(new Intent(getActivity(), BaseMultiPaneActivity.class));
//                } else {
//                    startActivity(new Intent(getActivity(), BaseSinglePaneActivity.class));
//                }
            }
        });

        root.findViewById(R.id.home_btn_announcements).setOnClickListener(
                new View.OnClickListener() {
                    public void onClick(View view) {
                        // splicing in tag streamer
//                        Intent intent = new Intent(getActivity(), BaseSinglePaneActivity.class);
//                        startActivity(intent);
                    }
                });

        return root;
    }
}
