package cn.xuhongxu.xiaoya.Fragment;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.avos.avoscloud.AVAnalytics;

import java.io.IOException;

import cn.xuhongxu.Assist.NeedLoginException;
import cn.xuhongxu.xiaoya.Adapter.EvaluationRecycleAdapter;
import cn.xuhongxu.xiaoya.Listener.RecyclerItemClickListener;
import cn.xuhongxu.xiaoya.R;
import cn.xuhongxu.xiaoya.YaApplication;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link EvaluationFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link EvaluationFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class EvaluationFragment extends Fragment {

    private OnFragmentInteractionListener mListener;
    private YaApplication app;

    private GetEvaluationTask task;
    private SwipeRefreshLayout swipeRefreshLayout;
    private RecyclerView recyclerView;
    private ProgressBar progressBar;

    public EvaluationFragment() {
        // Required empty public constructor
    }

    public static EvaluationFragment newInstance() {
        return new EvaluationFragment();
    }

    @Override
    public void onStart() {
        super.onStart();
        getActivity().setTitle(R.string.Evaluate);
    }

    private class GetEvaluationTask extends AsyncTask<Boolean, Void, String> {
        private boolean first = false;

        @Override
        protected void onPreExecute() {
            swipeRefreshLayout.setRefreshing(true);
        }

        @Override
        protected String doInBackground(Boolean... params) {
            if (params.length > 0) {
                first = params[0];
            }
            View view = getView();
            assert view != null;
            try {
                app.setEvaluationItemList(app.getAssist().getEvaluateList());
            } catch (NeedLoginException needLogin) {
                return getString(R.string.login_timeout);
            } catch (IOException e) {
                return getString(R.string.network_error);
            } catch (Exception e) {
                return e.getMessage();
            }
            return "";
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            progressBar.setIndeterminate(false);
            progressBar.setVisibility(View.GONE);
            if (result.equals("")) {
                updateItems(true, first);
            } else if (result.equals(getString(R.string.login_timeout)) || result.equals(getString(R.string.network_error))) {
                Toast.makeText(getContext(), result, Toast.LENGTH_LONG).show();
                mListener.onReLogin(false);
                swipeRefreshLayout.setRefreshing(false);
            } else {
                Toast.makeText(getContext(), result, Toast.LENGTH_LONG).show();
                swipeRefreshLayout.setRefreshing(false);
            }
        }
    }

    private void updateItems(boolean updated, boolean first) {
        if (updated) {
            EvaluationRecycleAdapter adapter =
                    new EvaluationRecycleAdapter(getActivity(), app.getEvaluationItemList());
            recyclerView.setAdapter(adapter);
        }

        View view = getView();
        if (view != null) {
            final SharedPreferences preferences =
                    getActivity().getSharedPreferences(getString(R.string.preference_key),
                            Context.MODE_PRIVATE);
            if (preferences.getBoolean("showEvaluationTip", true)) {
                int showTextId = R.string.select_evaluation_turn;
                if (!first) {
                    showTextId = R.string.updated;
                }
                Snackbar snackbar = Snackbar.make(view, showTextId, Snackbar.LENGTH_SHORT);
                if (first) {
                    snackbar.setAction(R.string.do_not_show_again, new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            SharedPreferences.Editor editor = preferences.edit();
                            editor.putBoolean("showEvaluationTip", false);
                            editor.apply();
                        }
                    });
                }
                snackbar.show();
            }
        }
        swipeRefreshLayout.setRefreshing(false);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_evaluation, container, false);

        app = (YaApplication) getActivity().getApplication();
        swipeRefreshLayout = (SwipeRefreshLayout) v.findViewById(R.id.evaluation_swipe_refresh_layout);
        recyclerView = (RecyclerView) v.findViewById(R.id.evaluation_recycler_view);

        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        layoutManager.scrollToPosition(0);
        recyclerView.setLayoutManager(layoutManager);
        EvaluationRecycleAdapter adapter =
                new EvaluationRecycleAdapter(getActivity(), app.getEvaluationItemList());
        recyclerView.setAdapter(adapter);
        recyclerView.addOnItemTouchListener(new RecyclerItemClickListener(getContext(),
                recyclerView,
                new RecyclerItemClickListener.OnItemClickListener() {
                    @Override
                    public void onItemClick(View view, int position) {
                        mListener.onEvaluateCourse(position);
                    }

                    @Override
                    public void onItemLongClick(View view, int position) {

                    }

                }));
        progressBar = (ProgressBar) v.findViewById(R.id.progressBar);
        if (app.getEvaluationItemList().isEmpty()) {
            progressBar.setIndeterminate(true);
            task = new GetEvaluationTask();
            task.execute(true);
        } else {
            progressBar.setVisibility(View.GONE);
            updateItems(false, true);
        }
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                task = new GetEvaluationTask();
                task.execute(false);
            }
        });

        return v;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
        if (task != null) {
            task.cancel(true);
        }
    }

    public interface OnFragmentInteractionListener {
        void onReLogin(boolean back);

        void onEvaluateCourse(int pos);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.only_refresh_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_refresh) {
            task = new GetEvaluationTask();
            task.execute(false);
        }

        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onPause() {
        super.onPause();
        AVAnalytics.onFragmentEnd(getClass().getName());
    }

    @Override
    public void onResume() {
        super.onResume();
        AVAnalytics.onFragmentStart(getClass().getName());
    }
}
