package com.oltean.android.busapp;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import jxl.Cell;
import jxl.Sheet;
import jxl.Workbook;
import jxl.read.biff.BiffException;

/**
 * Created by Andrei on 16/04/2015.
 */
public class ScheduleFragment extends Fragment {

    ArrayAdapter<String> busesAdapter;

    public ScheduleFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Add this line in order for this fragment to handle menu events
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.schedulefragment, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_refresh) {
            FetchTimesTask timesTask = new FetchTimesTask();
            timesTask.execute("25");
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        String[] busesArray = {
                "bus nr. 30",
                "bus nr. 25",
                "tram nr. 101"
        };

        List<String> buses = new ArrayList<String>(Arrays.asList(busesArray));

        busesAdapter = new ArrayAdapter<String>(
                getActivity(),
                R.layout.list_item_buses,
                R.id.list_item_buses_textview,
                buses
        );

        ListView listView = (ListView) rootView.findViewById(R.id.listview_buses);
        listView.setAdapter(busesAdapter);

        return rootView;
    }

    public class FetchTimesTask extends AsyncTask<String, Void, ArrayList<ArrayList<String>>> {

        private final String LOG_TAG = FetchTimesTask.class.getSimpleName();

        @Override
        protected ArrayList<ArrayList<String>> doInBackground(String... params) {
            HttpURLConnection urlConnection = null;
            Workbook workbook = null;

            String end1 = null, end2 = null;
            String time = null;
            String line = params[0];

            try {
                // Construct the URL for the bus schedule query
                URL url = new URL("http://www.ratuc.ro/orare/orar_" + line + ".xls");

                // Create the request to Ratuc and open the connection
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                // Read the input stream into a String
                InputStream inputStream = urlConnection.getInputStream();
                ArrayList<ArrayList<String>> times = new ArrayList<ArrayList<String>>(2);
                ArrayList<String> aux = new ArrayList<String>();
                times.add(aux);
                aux = new ArrayList<String>();
                times.add(aux);
                if (inputStream == null)
                    return null;
                try {
                    workbook = Workbook.getWorkbook(inputStream);
                    Sheet schedule = workbook.getSheet(0);
                    int x1 = 1, y1 = 3;
                    Cell line_start = schedule.getCell(x1, y1);
                    end1 = line_start.getContents();
                    int x2 = x1 + 1, y2 = y1;
                    Cell line_end = schedule.getCell(x2, y2);
                    while (line_end.getContents().equals("")) {
                        x2++;
                        line_end = schedule.getCell(x2, y2);
                    }
                    end2 = line_end.getContents();
                    Log.v(LOG_TAG, end1 + " " + end2);
                    for (int i = 4; i > 0; i--) {
                        x1 = line_start.getColumn();
                        y1 = line_start.getRow() + 1;
                        Cell time1 = schedule.getCell(x1, y1);
                        while ((time1.getContents()).equals("")) {
                            y1++;
                            time1 = schedule.getCell(x1, y1);
                        }
                        while ((time1.getContents()).equals("") == false) {
                            if ((time1.getContents()).equals("*") == false)
                                times.get(0).add(time1.getContents());
                            y1++;
                            time1 = schedule.getCell(x1, y1);
                        }
                        x2 = line_end.getColumn();
                        y2 = line_end.getRow() + 1;
                        Cell time2 = schedule.getCell(x2, y2);
                        while ((time2.getContents()).equals("")) {
                            y2++;
                            time2 = schedule.getCell(x2, y2);
                        }
                        while (!time2.getContents().equals("")) {
                            if (!time2.getContents().equals("*"))
                                times.get(1).add(time2.getContents());
                            y2++;
                            time2 = schedule.getCell(x2, y2);
                        }
                        x1++;
                        y1 = line_start.getRow();
                        while (x1 < schedule.getColumns()) {
                            line_start = schedule.getCell(x1, y1);
                            if (line_start.getContents().equals(end1))
                                break;
                            x1++;
                        }
                        x2++;
                        y2 = line_end.getRow();
                        while (x2 < schedule.getColumns()) {
                            line_end = schedule.getCell(x2, y2);
                            if (line_end.getContents().equals(end2))
                                break;
                            x2++;
                        }
                    }
//                    for (int j = 0; j < (times.get(0)).size(); j++)
                        Log.d(LOG_TAG, times.get(0).get(0));
//                    for (int j = 0; j < (times.get(1)).size(); j++)
                        Log.d(LOG_TAG, times.get(1).get(0));
                } catch (BiffException e) {
                    Log.e(LOG_TAG, "Error", e);
                    e.printStackTrace();
                }

            } catch (IOException e) {
                Log.e(LOG_TAG, "Error", e);
            } finally {
                if (urlConnection != null)
                    urlConnection.disconnect();
                if (workbook != null)
                    workbook.close();
            }

            return null;
        }
    }
}