package us.halex.deskdroid;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.LinearSnapHelper;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SnapHelper;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by HAlexTM on 07/09/2018 11:42
 */
public class HomeFragment extends Fragment {
    private Menu menu;
    private RecyclerView appsView;
    private AppsAdapter appsAdapter;
    private LinearLayoutManager layoutManager;
    private CardView currentApp;
    private int selectedApp = -1;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("apps_position", selectedApp);
    }

    @Override
    public void onPause() {
        super.onPause();
        Context context = getContext();
        App app = appsAdapter.getAppByPosition(selectedApp);
        if (context != null) {
            String name = app != null ? app.getName() : null;
            context.getSharedPreferences(getString(R.string.apps_info), Context.MODE_PRIVATE)
                    .edit()
                    .putString(getString(R.string.last_app_name), name)
                    .apply();

            /*Notification notification = new NotificationCompat.Builder(context, "default")
                    .setSmallIcon(R.drawable.ic_firefox_logo)
                    .setContentTitle("Settings saved!")
                    .setContentText("Last app name set to: " + name)
                    .build();
            NotificationManagerCompat.from(context).notify(0, notification);*/
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        // Installed apps list
        layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
        appsAdapter = new AppsAdapter();
        appsView = view.findViewById(R.id.apps);
        appsView.setLayoutManager(layoutManager);
        appsView.setAdapter(appsAdapter);

        SnapHelper snapHelper = new LinearSnapHelper() {
            @Override
            public View findSnapView(RecyclerView.LayoutManager layoutManager) {
                View view = super.findSnapView(layoutManager);
                if (view != null) {
                    int position = layoutManager.getPosition(view);
                    if (position != selectedApp && appsView.getScrollState() == RecyclerView.SCROLL_STATE_IDLE) {
                        onAppSelected(position, view);
                        selectedApp = position;
                    }
                }
                return view;
            }
        };
        snapHelper.attachToRecyclerView(appsView);

        if (savedInstanceState != null && savedInstanceState.containsKey(getString(R.string.apps_position))) {
            int pos = savedInstanceState.getInt(getString(R.string.apps_position));
            if (pos > -1) {
                appsView.smoothScrollToPosition(pos);
                selectedApp = pos;
            }
        } else {
            Context context = getContext();
            if (context != null) {
                String lastUsedApp = context.getSharedPreferences(getString(R.string.apps_info), Context.MODE_PRIVATE).getString(getString(R.string.last_app_name), null);
                appsAdapter.scrollToAppName(lastUsedApp);
            }
        }

        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.main, menu);
        this.menu = menu;
        menu.findItem(R.id.action_control).setOnMenuItemClickListener(item -> {
            if (currentApp != null && selectedApp > -1) {
                App app = appsAdapter.getAppByPosition(selectedApp);
                if (app != null) {
                    DeskDroidApp.openApp(getActivity(), app);
                    return true;
                }
            }
            Toast.makeText(getContext(), "Please, report error X4 to support@halex.us", Toast.LENGTH_LONG).show();
            if (getContext() != null) {
                new AlertDialog.Builder(getContext())
                        .setTitle("Error")
                        .setMessage("Please, report error X4 to support@halex.me")
                        .create()
                        .show();
            }
            return false;
        });
        setMenuText();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection RedundantIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return false;
    }

    private void onAppSelected(int position, View view) {
        if (!view.equals(currentApp) && position != selectedApp) {
            selectApp(position, (CardView) view);
        }
    }

    private void selectApp(int position, CardView card) {
        CardView oldCard = currentApp;
        currentApp = card;
        selectedApp = position;
        float elevationSelected = getResources().getDimension(R.dimen.elevation_selected);
        float elevationNormal = getResources().getDimension(R.dimen.elevation_normal);

        if (oldCard != null) {
            ObjectAnimator.ofFloat(oldCard, "elevation", elevationNormal).start();
        }
        ObjectAnimator.ofFloat(card, "elevation", elevationSelected).start();

        setMenuText();
    }

    private void setMenuText() {
        if (menu != null && currentApp != null) {
            MenuItem action = menu.findItem(R.id.action_control);
            App app = appsAdapter.getAppByPosition(selectedApp);
            if (app != null) {
                boolean running = DeskDroidApp.isRunning(app);
                action.setTitle(running ? R.string.action_stop : R.string.action_start).setVisible(true);
            } else {
                action.setTitle(R.string.there_was_error).setVisible(true);
            }
        }
    }


    private class AppsAdapter extends RecyclerView.Adapter<AppsAdapter.AppViewHolder> {
        List<App> apps = new ArrayList<>();

        private AppsAdapter() {
            apps.add(new App(getString(R.string.intellij), getResources().getDrawable(R.drawable.ic_intellij_logo, null), getResources().getDrawable(R.drawable.ic_intellij_logo_text, null)));
            apps.add(new App(getString(R.string.notepad), getResources().getDrawable(R.drawable.ic_notepad_logo, null), getResources().getDrawable(R.drawable.ic_intellij_logo_text, null)));
            apps.add(new App(getString(R.string.notepadpp), getResources().getDrawable(R.drawable.ic_notepadpp_logo, null), getResources().getDrawable(R.drawable.ic_intellij_logo_text, null)));
            apps.add(new App(getString(R.string.firefox), getResources().getDrawable(R.drawable.ic_firefox_logo, null), getResources().getDrawable(R.drawable.ic_intellij_logo_text, null)));
        }

        private App getAppByPosition(int pos) {
            return apps.size() > pos && pos > -1 ? apps.get(pos) : null;
        }

        private void scrollToAppName(@Nullable String name) {
            for (int i = 0; i < apps.size(); i++) {
                if (apps.get(i).getName().equalsIgnoreCase(name)) {
                    scrollTo(i);
                    return;
                }
            }
            scrollTo(0);
        }

        private void scrollTo(int pos) {
            appsView.smoothScrollToPosition(pos);
            selectedApp = pos;
        }

        @Override
        public int getItemCount() {
            return apps.size();
        }

        @NonNull
        @Override
        public AppViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
            View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.card_app, viewGroup, false);
            v.setElevation(4);
            v.setOnClickListener(view -> {
                int position = layoutManager.getPosition(v);
                if (currentApp != view) {
                    appsView.smoothScrollToPosition(position);
                    selectApp(position, (CardView) view);
                }
            });
            return new AppViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull AppViewHolder holder, int i) {
            App app = apps.get(i);
            holder.name.setText(app.getName());
            holder.image.setImageDrawable(app.getLogo());
            if (selectedApp == i) {
                selectApp(i, (CardView) holder.itemView);
            }
        }

        class AppViewHolder extends RecyclerView.ViewHolder {
            TextView name;
            ImageView image;

            AppViewHolder(View itemView) {
                super(itemView);
                name = itemView.findViewById(R.id.app_name);
                image = itemView.findViewById(R.id.app_image);
            }
        }
    }
}
