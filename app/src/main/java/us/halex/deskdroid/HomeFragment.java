package us.halex.deskdroid;

import android.animation.ObjectAnimator;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
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
    private LinearLayoutManager layoutManager;
    private CardView previousApp;
    private int selectedApp = -1;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        Toast.makeText(getContext(), "onCreateView()", Toast.LENGTH_SHORT).show();

        // Installed apps list
        appsView = view.findViewById(R.id.apps);
        layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
        appsView.setLayoutManager(layoutManager);
        appsView.setAdapter(new AppsAdapter());

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


        //
        // Select last used app...
        appsView.smoothScrollToPosition(2);
        // ^^^

        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.main, menu);
        this.menu = menu;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void onAppSelected(int position, View view) {
        // Ignore duplicate
        if (view.equals(previousApp) || position == selectedApp) return;

        CardView card = (CardView) view;
        TextView text = card.findViewById(R.id.app_name);
        float elevationSelected = getResources().getDimension(R.dimen.elevation_selected);
        float elevationNormal = getResources().getDimension(R.dimen.elevation_normal);

        //card.setElevation(10);
        ObjectAnimator.ofFloat(card, "elevation", elevationSelected).start();
        if (previousApp != null) {
            ObjectAnimator.ofFloat(previousApp, "elevation", elevationNormal).start();
        }

        menu.findItem(R.id.action_control).setTitle(":" + text.getText()).setVisible(true);
        previousApp = card;
    }










    private class AppsAdapter extends RecyclerView.Adapter<AppsAdapter.AppViewHolder> {
        List<App> apps = new ArrayList<>();

        private AppsAdapter() {
            apps.add(new App(getContext(), R.string.intellij, R.drawable.ic_intellij_logo, R.drawable.ic_intellij_logo_text));
            apps.add(new App(getContext(), R.string.intellij, R.drawable.ic_intellij_logo, R.drawable.ic_intellij_logo_text));
            apps.add(new App(getContext(), R.string.intellij, R.drawable.ic_intellij_logo, R.drawable.ic_intellij_logo_text));
            apps.add(new App(getContext(), R.string.intellij, R.drawable.ic_intellij_logo, R.drawable.ic_intellij_logo_text));
            apps.add(new App(getContext(), R.string.intellij, R.drawable.ic_intellij_logo, R.drawable.ic_intellij_logo_text));
            apps.add(new App(getContext(), R.string.intellij, R.drawable.ic_intellij_logo, R.drawable.ic_intellij_logo_text));
            apps.add(new App(getContext(), R.string.intellij, R.drawable.ic_intellij_logo, R.drawable.ic_intellij_logo_text));
            apps.add(new App(getContext(), R.string.intellij, R.drawable.ic_intellij_logo, R.drawable.ic_intellij_logo_text));
            apps.add(new App(getContext(), R.string.intellij, R.drawable.ic_intellij_logo, R.drawable.ic_intellij_logo_text));
            apps.add(new App(getContext(), R.string.intellij, R.drawable.ic_intellij_logo, R.drawable.ic_intellij_logo_text));
        }

        public class AppViewHolder extends RecyclerView.ViewHolder {
            TextView name;
            ImageView image;

            AppViewHolder(View itemView) {
                super(itemView);
                name = itemView.findViewById(R.id.app_name);
                image = itemView.findViewById(R.id.app_image);
            }
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
                Toast.makeText(getContext(), "You've clicked on: " + ((TextView) view.findViewById(R.id.app_name)).getText(), Toast.LENGTH_SHORT).show();
                int position = layoutManager.getPosition(v);
                if (previousApp != view) {
                    Toast.makeText(getContext(), "Not the selected item, changing...", Toast.LENGTH_SHORT).show();
                    appsView.smoothScrollToPosition(position);
                    onAppSelected(position, view);
                }
            });
            return new AppViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull AppViewHolder holder, int i) {
            App app = apps.get(i);
            holder.name.setText(app.getName());
            holder.image.setImageDrawable(app.getLogo());
        }
    }
}
