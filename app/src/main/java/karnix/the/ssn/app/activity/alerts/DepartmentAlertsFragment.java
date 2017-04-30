package karnix.the.ssn.app.activity.alerts;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ProgressBar;
import android.widget.Spinner;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import karnix.the.ssn.app.adapters.PostAdapter;
import karnix.the.ssn.app.model.Node;
import karnix.the.ssn.app.model.posts.WebConsolePost;
import karnix.the.ssn.app.utils.LogHelper;
import karnix.the.ssn.ssnmachan.R;

public class DepartmentAlertsFragment extends Fragment {

    private static final String TAG = LogHelper.makeLogTag(DepartmentAlertsFragment.class);

    @BindView(R.id.postsRecyclerView)
    RecyclerView postsRecyclerView;
    @BindView(R.id.progressBar)
    ProgressBar progressBar;
    @BindView(R.id.spinner_department)
    Spinner spinnerDepartment;

    private Unbinder unbinder;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_alerts_department, container, false);
        unbinder = ButterKnife.bind(this, rootView);

        final String[] departmentKeys = {"cse", "ece", "eee", "mech", "it", "chem", "biomed", "civil"};

        spinnerDepartment.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                progressBar.setVisibility(View.VISIBLE);

                final String type = departmentKeys[spinnerDepartment.getSelectedItemPosition()];

                final LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
                layoutManager.setStackFromEnd(true);
                layoutManager.setReverseLayout(true);

                final List<WebConsolePost> postList = new ArrayList<>();
                final PostAdapter postAdapter = new PostAdapter(getActivity(), postList);
                postsRecyclerView.setLayoutManager(layoutManager);
                postsRecyclerView.setAdapter(postAdapter);

                final FirebaseDatabase database = FirebaseDatabase.getInstance();
                DatabaseReference nodesRef = database.getReference("categorywise_posts/" + type);
                final ValueEventListener valueEventListener = new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        WebConsolePost post = dataSnapshot.getValue(WebConsolePost.class);
                        postList.add(post);
                        postAdapter.notifyDataSetChanged();
                        layoutManager.scrollToPositionWithOffset(postList.size() - 1, 0);
                        if (progressBar != null) {
                            progressBar.setVisibility(View.GONE);
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                    }
                };
                ChildEventListener childEventListener = new ChildEventListener() {
                    @Override
                    public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                        Node node = dataSnapshot.getValue(Node.class);
                        DatabaseReference nodesRef = database.getReference("posts/" + node.getPid());
                        nodesRef.addValueEventListener(valueEventListener);
                    }

                    @Override
                    public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                    }

                    @Override
                    public void onChildRemoved(DataSnapshot dataSnapshot) {
                    }

                    @Override
                    public void onChildMoved(DataSnapshot dataSnapshot, String s) {
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                    }
                };
                nodesRef.addChildEventListener(childEventListener);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        ArrayAdapter arrayAdapter = ArrayAdapter.createFromResource(getActivity(),
                R.array.department_category_keys, android.R.layout.simple_spinner_dropdown_item);
        spinnerDepartment.setAdapter(arrayAdapter);

        return rootView;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }
}
