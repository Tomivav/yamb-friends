package com.plazonic.tomislav.yambfriends;

import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GameActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        final Dice dice = new Dice(5);
        final int[] diceIds = {R.id.diceView1, R.id.diceView2, R.id.diceView3, R.id.diceView4, R.id.diceView5, R.id.diceView6};
        final Map<String, ImageView> ivDice = new HashMap<>(dice.getQuantity(), 1);
        if (dice.getQuantity() < 6) ((ViewGroup) findViewById(R.id.diceView6).getParent()).removeView(findViewById(R.id.diceView6));
        for (int i = 0; i < dice.getQuantity(); i++) {
            ivDice.put("ivDice" + (i + 1), (ImageView) findViewById(diceIds[i]));
            ivDice.get("ivDice" + (i + 1)).setTag(false);
        }

        final TextView tvRollNo = (TextView) findViewById(R.id.rollNo);
        tvRollNo.setText(String.format("%d", dice.getRollNumber()));

        final Chronometer cmTimer = (Chronometer) findViewById(R.id.timer);
        cmTimer.setTag(false);

        final Grid grid = new Grid(false); // change constant to input value
        final GridView gvGrid = (GridView) findViewById(R.id.gridView);
        gvGrid.setNumColumns(grid.getNumOfCols(false));

        ListAdapter gvAdapter = new ArrayAdapter<>(this, R.layout.grid_cell, grid.getListCells());
        gvGrid.setAdapter(gvAdapter);
        gvGrid.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String cellName = grid.positionToCellName(position);
                int nCol = grid.getNumOfCols(false);
                if (position / nCol == 0 || position % nCol == 0) {
                    String text = getResources().getString(getResources().getIdentifier("_" + cellName, "string", getPackageName()));
                    Toast.makeText(getApplicationContext(), text, Toast.LENGTH_LONG).show();
                } else if (grid.getAvailableCells().contains(cellName) && dice.getRollNumber() > 0) {
                    if (dice.getRollNumber() == 1 && grid.getCellColName(cellName).equals("an1") && grid.getAnnouncedCellName() == null) {
                        // ...
                        grid.setAnnouncedCellName(cellName);
                    } else {
                        int result = dice.calculateInput(grid.getCellRowName(cellName));
                        grid.setModelValue(cellName, result);
                        ((TextView) view).setText(String.format("%d", result));
                        grid.setLastInputCellName(cellName);
                        grid.setInputDone(true);
                        // ...
                    }

                    grid.checkCompletedSections();
                    if (!grid.getLastSumCellsNames().isEmpty()) {
                        List<String> lastSumCellsNames = grid.getLastSumCellsNames();
                        TextView tv;
                        // update view with lastSumCellsNames
                        for (int i = 0; i < lastSumCellsNames.size(); i++) {
                            tv = (TextView) gvGrid.getItemAtPosition(grid.cellNameToPosition(lastSumCellsNames.get(i)));
                            tv.setText(String.format("%d", grid.getModelValue(lastSumCellsNames.get(i))));
                        }
                    }

                    if (grid.isGameFinished()) {
                        grid.calculateFinalResult();
                        Toast.makeText(getApplicationContext(), "Final result: " + grid.getFinalResult(), Toast.LENGTH_LONG).show();
                    }
                }
            }
        });

        Button btnRoll = (Button) findViewById(R.id.rollBtn);
        btnRoll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!grid.isGameFinished()) {
                    // temp
                    if (!(boolean) cmTimer.getTag()) {
                        cmTimer.setBase(SystemClock.elapsedRealtime());
                        cmTimer.start();
                        cmTimer.setTag(true);
                    } else {
                        cmTimer.stop();
                        cmTimer.setTag(false);
                    }

                    if (!grid.getInputDone() && dice.getRollNumber() == 3) {
                        Toast.makeText(getApplicationContext(), "Input required!", Toast.LENGTH_SHORT).show();
                    }

                    if (grid.getInputDone()) {
                        for (int i = 0; i < dice.getQuantity(); i++) {
                            ivDice.get("ivDice" + (i + 1)).setTag(false);
                            ivDice.get("ivDice" + (i + 1)).clearColorFilter();
                        }
                        dice.setRollNumber(0);
                        grid.setInputDone(false);
                        grid.setAnnouncedCellName(null);
                    }

                    if (dice.getRollNumber() < 3 && !grid.getInputDone()) {
                        if (dice.getRollNumber() == 1 && grid.onlyLeftAn1() && grid.getAnnouncedCellName() == null) {
                            Toast.makeText(getApplicationContext(), "Announcement required!", Toast.LENGTH_SHORT).show();
                        } else {
                            dice.incrementRollNumber();
                            tvRollNo.setText(String.format("%d", dice.getRollNumber()));
                            for (int i = 0; i < dice.getQuantity(); i++) {
                                if (!(boolean) ivDice.get("ivDice" + (i + 1)).getTag()) {
                                    int newRandom = dice.getRandom();
                                    dice.setDice(i, newRandom);
                                    ivDice.get("ivDice" + (i + 1)).setImageResource(getResources().getIdentifier("dice_" + newRandom, "drawable", getPackageName()));
                                }
                            }

                            if (grid.getAnnouncedCellName() == null) {
                                // onlyAnn = false;
                                // new available cells
                                // if (onlyAnn) onlyAnn = true;
                            }
                        }
                    }

                    tvRollNo.setText(String.format("%d", dice.getRollNumber()));
                }
            }
        });

        Button btnUndo = (Button) findViewById(R.id.undoBtn);
        btnUndo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!grid.isGameFinished()) {
                    if (grid.getInputDone()) {
                        grid.setModelValue(grid.getLastInputCellName(), -1);
                        // update view @GameActivity

                        if (grid.getAnnouncedCellName() == null) {
                            // available ...
                        } else {
                            // available announced ...
                        }

                        if (grid.getLastSumCellsNames().size() > 0) {
                            for (int i = 0; i < grid.getLastSumCellsNames().size(); i++) {
                                grid.setModelValue(grid.getLastSumCellsNames().get(i), -1);
                                // update view @GameActivity
                            }
                        }

                        grid.setInputDone(false);
                    }

                    if (dice.getRollNumber() == 1 && grid.getAnnouncedCellName() != null) {

                    }
                }
            }
        });
    }

    public void diceClick(View v) {
        ImageView iv = (ImageView) v;
        if ((boolean) iv.getTag()) {
            iv.clearColorFilter();
            iv.setTag(false);
        } else {
            iv.setColorFilter(R.color.diceGray);
            iv.setTag(true);
        }
    }

}
