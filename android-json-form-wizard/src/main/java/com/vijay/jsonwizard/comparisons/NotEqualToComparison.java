package com.vijay.jsonwizard.comparisons;

import android.util.Log;

import com.vijay.jsonwizard.utils.Utils;
import com.vijay.jsonwizard.widgets.DatePickerFactory;

import org.json.JSONArray;
import org.json.JSONException;
import org.smartregister.client.utils.domain.Form;

import java.util.ArrayList;
import java.util.Date;

public class NotEqualToComparison extends Comparison {
    private static final String TAG = "NotEqualToComparison";

    @Override
    public boolean compare(String a, String type, String b) {
        try {
            switch (type) {
                case TYPE_STRING:
                    if (a == null) {
                        a = DEFAULT_STRING;
                    }
                    return a.compareTo(b) != 0;
                case TYPE_NUMERIC:
                    if (a == null) {
                        a = DEFAULT_NUMERIC;
                    }
                    if (b == null) {
                        b = DEFAULT_NUMERIC;
                    }
                    return !Double.valueOf(a).equals(Double.valueOf(b));
                case TYPE_DATE:
                    if (a == null) {
                        a = DEFAULT_DATE;
                    }
                    if (b == null) {
                        b = DEFAULT_DATE;
                    }
                    Date dateA = DatePickerFactory.DATE_FORMAT.parse(Utils.getDateFormattedForCalculation(a,  Form.getDatePickerDisplayFormat()));
                    Date dateB = DatePickerFactory.DATE_FORMAT.parse(b);
                    return dateA.getTime() != dateB.getTime();
                case TYPE_ARRAY:
                    if (a == null) {
                        a = DEFAULT_ARRAY;
                    }
                    if (b == null) {
                        b = DEFAULT_ARRAY;
                    }

                    // An array is only equal to another if they have the same number of items
                    // and all these items are in both arrays
                    try {
                        JSONArray aArray = new JSONArray(a);
                        JSONArray bArray = new JSONArray(b);

                        ArrayList<String> aList1 = new ArrayList<>();
                        for (int i = 0; i < aArray.length(); i++) {
                            aList1.add(aArray.getString(i));
                        }
                        ArrayList<String> aList2 = new ArrayList<>(aList1);

                        ArrayList<String> bList1 = new ArrayList<>();
                        for (int i = 0; i < bArray.length(); i++) {
                            bList1.add(bArray.getString(i));
                        }
                        ArrayList<String> bList2 = new ArrayList<>(bList1);

                        aList1.removeAll(bList2);
                        bList1.removeAll(aList2);

                        return aList1.size() == aList2.size() && bList1.size() == bList2.size();
                    } catch (JSONException e) {
                        Log.e(TAG, Log.getStackTraceString(e));
                    }
                    return false;
                default:
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    @Override
    public String getFunctionName() {
        return "notEqualTo";
    }
}
