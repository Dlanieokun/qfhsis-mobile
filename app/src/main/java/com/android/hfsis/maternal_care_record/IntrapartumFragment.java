package com.android.hfsis.maternal_care_record;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;

import com.android.hfsis.R;
import com.android.hfsis.database.DatabaseHelper;
import com.android.hfsis.model.maternal_care_record.IntrapartumEntity;
import com.google.android.material.textfield.TextInputLayout;

import java.util.Calendar;
import java.util.Locale;
import java.util.concurrent.Executors;

public class IntrapartumFragment extends Fragment {

    private Spinner spinnerOutcome, spinnerType, spinnerPlace, spinnerAttendant, spinnerWeightClass;
    private EditText etWeight, etDate, etTime, etRemarks;
    private Button btnSave;
    private RadioGroup rgSex;
    private int maternalRecordId = -1;

    public IntrapartumFragment() {}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_intrapartum, container, false);

        setupKeyboardInsets(view);

        // Retrieve the Maternal ID passed from the list fragment
        if (getArguments() != null) {
            maternalRecordId = getArguments().getInt("MATERNAL_RECORD_ID", -1);
        }

        initializeViews(view);
        setupSpinners();
        setupDateTimePickers();

        // Load data if a record exists for this ID
        if (maternalRecordId != -1) {
            loadExistingData();
        }

        btnSave.setOnClickListener(v -> saveOrUpdateData());

        return view;
    }

    /**
     * Keeps the focused input visible above the soft keyboard.
     * Works on edge-to-edge (Android 15+) where adjustResize alone is not enough.
     */
    private void setupKeyboardInsets(@NonNull View root) {
        final int baseLeft = root.getPaddingLeft();
        final int baseTop = root.getPaddingTop();
        final int baseRight = root.getPaddingRight();
        final int baseBottom = root.getPaddingBottom();

        ViewCompat.setOnApplyWindowInsetsListener(root, (v, insets) -> {
            Insets ime = insets.getInsets(WindowInsetsCompat.Type.ime());
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            int bottom = Math.max(ime.bottom, bars.bottom);
            v.setPadding(baseLeft, baseTop, baseRight, baseBottom + bottom);

            if (ime.bottom > 0) {
                View focused = v.findFocus();
                if (focused != null) {
                    v.post(() -> scrollToView(v, focused));
                }
            }
            return insets;
        });

        // Scroll to a field whenever it gains focus while the keyboard is open
        root.getViewTreeObserver().addOnGlobalFocusChangeListener((oldFocus, newFocus) -> {
            if (newFocus instanceof EditText) {
                root.postDelayed(() -> scrollToView(root, newFocus), 200);
            }
        });

        ViewCompat.requestApplyInsets(root);
    }

    private void scrollToView(View root, View target) {
        if (!(root instanceof ScrollView) || !isAdded()) return;
        ScrollView scroll = (ScrollView) root;
        android.graphics.Rect rect = new android.graphics.Rect();
        target.getDrawingRect(rect);
        scroll.offsetDescendantRectToMyCoords(target, rect);
        rect.bottom += (int) (80 * getResources().getDisplayMetrics().density);
        scroll.requestChildRectangleOnScreen(scroll.getChildAt(0), rect, false);
    }

    private void initializeViews(View view) {
        spinnerOutcome = view.findViewById(R.id.spinner_delivery_outcome);
        spinnerWeightClass = view.findViewById(R.id.spinner_weight_class);
        spinnerType = view.findViewById(R.id.spinner_delivery_type);
        spinnerPlace = view.findViewById(R.id.spinner_place_delivery);
        spinnerAttendant = view.findViewById(R.id.spinner_attendant);
        etWeight = view.findViewById(R.id.et_weight);
        etDate = view.findViewById(R.id.et_delivery_date);
        etTime = view.findViewById(R.id.et_delivery_time);
        etRemarks = view.findViewById(R.id.et_remarks);
        btnSave = view.findViewById(R.id.btn_save);
        rgSex = view.findViewById(R.id.rg_sex);
    }

    private void loadExistingData() {
        Executors.newSingleThreadExecutor().execute(() -> {
            IntrapartumEntity existing = DatabaseHelper.getInstance(getContext())
                    .intrapartumDao().getRecordByMaternalId(maternalRecordId);

            if (existing != null && getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    if (!isAdded()) return;

                    etWeight.setText(existing.birthWeight);
                    etDate.setText(existing.deliveryDate);
                    etTime.setText(existing.deliveryTime);
                    etRemarks.setText(existing.remarks);

                    setSpinnerSelection(spinnerOutcome, existing.deliveryOutcome);
                    setSpinnerSelection(spinnerType, existing.deliveryType);
                    setSpinnerSelection(spinnerPlace, existing.placeOfDelivery);
                    setSpinnerSelection(spinnerAttendant, existing.attendantAtBirth);
                    setSpinnerSelection(spinnerWeightClass, existing.weightClassification);

                    if (existing.sex != null) {
                        if (existing.sex.equals("M - Male")) {
                            rgSex.check(R.id.rb_male);
                        } else if (existing.sex.equals("F - Female")) {
                            rgSex.check(R.id.rb_female);
                        }
                    }
                });
            }
        });
    }

    @SuppressWarnings("unchecked")
    private void setSpinnerSelection(Spinner spinner, String value) {
        if (value == null || value.isEmpty()) return;

        ArrayAdapter<String> adapter = (ArrayAdapter<String>) spinner.getAdapter();
        for (int i = 0; i < adapter.getCount(); i++) {
            if (adapter.getItem(i).equals(value)) {
                spinner.setSelection(i);
                break;
            }
        }
    }

    private void saveOrUpdateData() {
        if (maternalRecordId == -1) {
            Toast.makeText(getContext(), "Error: Missing Maternal ID", Toast.LENGTH_SHORT).show();
            return;
        }

        // Read all UI values on the main thread
        final String sexValue;
        int selectedId = rgSex.getCheckedRadioButtonId();
        if (selectedId != -1) {
            RadioButton selectedButton = getView().findViewById(selectedId);
            sexValue = selectedButton.getText().toString();
        } else {
            sexValue = "";
        }

        final String weightValue = etWeight.getText().toString();
        final String dateValue = etDate.getText().toString();
        final String timeValue = etTime.getText().toString();
        final String remarksValue = etRemarks.getText().toString();
        final String outcomeValue = spinnerOutcome.getSelectedItem().toString();
        final String typeValue = spinnerType.getSelectedItem().toString();
        final String placeValue = spinnerPlace.getSelectedItem().toString();
        final String attendantValue = spinnerAttendant.getSelectedItem().toString();
        final String weightClassValue = spinnerWeightClass.getSelectedItem().toString();

        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                DatabaseHelper db = DatabaseHelper.getInstance(getContext());
                IntrapartumEntity existing = db.intrapartumDao().getRecordByMaternalId(maternalRecordId);

                IntrapartumEntity entity = (existing != null) ? existing : new IntrapartumEntity();
                entity.maternalRecordId = maternalRecordId;
                entity.sex = sexValue;
                entity.birthWeight = weightValue;
                entity.deliveryDate = dateValue;
                entity.deliveryTime = timeValue;
                entity.remarks = remarksValue;
                entity.deliveryOutcome = outcomeValue;
                entity.deliveryType = typeValue;
                entity.placeOfDelivery = placeValue;
                entity.attendantAtBirth = attendantValue;
                entity.weightClassification = weightClassValue;
                entity.isSynced = false; // mark dirty so Upload picks it up
                entity.updatedAt = System.currentTimeMillis();

                if (existing == null) {
                    entity.newInsert = true;
                    db.intrapartumDao().insertIntrapartum(entity);
                } else {
                    db.intrapartumDao().updateIntrapartum(entity);
                }

                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        Toast.makeText(getContext(), existing == null ? "Saved" : "Updated", Toast.LENGTH_SHORT).show();
                        getParentFragmentManager().popBackStack();
                    });
                }
            } catch (Exception e) {
                e.printStackTrace();
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() ->
                            Toast.makeText(getContext(), "Save failed: " + e.getMessage(), Toast.LENGTH_LONG).show());
                }
            }
        });
    }

    private void setupSpinners() {
        String[] outcomes = {"FT - Full Term", "PT - Pre-term", "FD - Fetal Death", "AB - Abortion"};
        String[] types = {"CS - Cesarean Section", "VD - Vaginal Delivery", "CVCD - Combined Delivery"};
        setupSpinnerAdapter(spinnerOutcome, outcomes);
        setupSpinnerAdapter(spinnerType, types);
        setupSpinnerAdapter(spinnerWeightClass, new String[]{"A - Normal (>2500g)", "B - Low (<2500g)", "C - Unknown"});
        setupSpinnerAdapter(spinnerPlace, new String[]{"Public Facility", "Private Facility", "Home", "Others"});
        setupSpinnerAdapter(spinnerAttendant, new String[]{"MD - Doctor", "RN - Nurse", "MW - Midwife", "O - Others"});
    }

    private void setupSpinnerAdapter(Spinner spinner, String[] data) {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, data);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
    }

    private TextInputLayout findParentTextInputLayout(View child) {
        ViewParent parent = child.getParent();
        while (parent != null) {
            if (parent instanceof TextInputLayout) return (TextInputLayout) parent;
            parent = parent.getParent();
        }
        return null;
    }

    private void setupDateTimePickers() {
        // Formats keyboard inputs while typing manually
        etDate.addTextChangedListener(new DateFormattingWatcher(etDate));

        // Opens calendar date picker only when clicking the end icon
        TextInputLayout tilDate = findParentTextInputLayout(etDate);
        if (tilDate != null) {
            tilDate.setEndIconOnClickListener(v -> {
                Calendar c = Calendar.getInstance();
                new DatePickerDialog(requireContext(), (view, y, m, d) -> {
                    etDate.setText(String.format(Locale.US, "%02d-%02d-%04d", m + 1, d, y));
                    etDate.setError(null);
                }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show();
            });
        }

        etTime.setOnClickListener(v -> {
            Calendar c = Calendar.getInstance();
            new TimePickerDialog(requireContext(), (view, h, m) ->
                    etTime.setText(String.format(Locale.US, "%02d:%02d", h, m)),
                    c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE), true).show();
        });
    }

    private static class DateFormattingWatcher implements TextWatcher {
        private final EditText editText;
        private boolean isDeleting = false;

        public DateFormattingWatcher(EditText editText) {
            this.editText = editText;
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            isDeleting = count > after;
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {}

        @Override
        public void afterTextChanged(Editable s) {
            if (isDeleting) {
                if (s.length() == 3 || s.length() == 6) {
                    s.delete(s.length() - 1, s.length());
                }
                return;
            }

            String digits = s.toString().replaceAll("[^\\d]", "");
            StringBuilder formatted = new StringBuilder();

            int len = digits.length();
            if (len > 8) len = 8;

            for (int i = 0; i < len; i++) {
                formatted.append(digits.charAt(i));
                if ((i == 1 && len > 2) || (i == 3 && len > 4)) {
                    formatted.append("-");
                }
            }

            editText.removeTextChangedListener(this);
            editText.setText(formatted.toString());
            editText.setSelection(formatted.length());
            editText.addTextChangedListener(this);
        }
    }
}