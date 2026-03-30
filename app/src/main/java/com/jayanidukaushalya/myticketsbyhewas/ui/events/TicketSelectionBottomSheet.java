package com.jayanidukaushalya.myticketsbyhewas.ui.events;

import android.os.Bundle;
import android.view.LayoutInflater;
import org.json.JSONArray;
import org.json.JSONObject;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.snackbar.Snackbar;
import com.jayanidukaushalya.myticketsbyhewas.R;
import com.jayanidukaushalya.myticketsbyhewas.data.model.Event;
import com.jayanidukaushalya.myticketsbyhewas.data.model.EventDate;
import com.jayanidukaushalya.myticketsbyhewas.data.model.EventTicket;
import com.jayanidukaushalya.myticketsbyhewas.data.model.EventTimeSlot;
import com.jayanidukaushalya.myticketsbyhewas.databinding.BottomSheetTicketSelectionBinding;
import com.jayanidukaushalya.myticketsbyhewas.databinding.ItemSelectionRowBinding;
import com.jayanidukaushalya.myticketsbyhewas.databinding.ItemTicketRowBinding;
import com.jayanidukaushalya.myticketsbyhewas.viewmodel.EventViewModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class TicketSelectionBottomSheet extends BottomSheetDialogFragment {

    public static final String TAG = "TicketSelectionBottomSheet";
    public static final String RESULT_KEY = "ticket_selection_result";

    private static final int STEP_DATE   = 0;
    private static final int STEP_TIME   = 1;
    private static final int STEP_TICKET = 2;

    private BottomSheetTicketSelectionBinding binding;
    private Event event;
    private int currentStep = -1;

    private EventDate selectedDate;
    private EventTimeSlot selectedSlot;
    private List<TicketQuantity> ticketQuantities;

    // ── Factory ──────────────────────────────────────────────────────────────

    public static TicketSelectionBottomSheet newInstance() {
        return new TicketSelectionBottomSheet();
    }

    // ── Lifecycle ────────────────────────────────────────────────────────────

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = BottomSheetTicketSelectionBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        EventViewModel viewModel = new ViewModelProvider(requireParentFragment())
                .get(EventViewModel.class);
        event = viewModel.getSelectedEvent().getValue();

        if (event == null) {
            dismiss();
            return;
        }

        binding.recyclerSelection.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.buttonBack.setOnClickListener(v -> goBack());

        navigateToFirstStep();
    }

    @Override
    public void onStart() {
        super.onStart();
        if (getDialog() == null) return;
        View bottomSheet = getDialog().findViewById(com.google.android.material.R.id.design_bottom_sheet);
        if (bottomSheet != null) {
            BottomSheetBehavior.from(bottomSheet).setState(BottomSheetBehavior.STATE_EXPANDED);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    // ── Navigation ───────────────────────────────────────────────────────────

    private void navigateToFirstStep() {
        String type = scheduleType();
        if ("multi_day".equals(type)) {
            showStep(STEP_DATE);
        } else if ("multi_time".equals(type)) {
            selectedDate = firstDate();
            showStep(STEP_TIME);
        } else {
            selectedDate = firstDate();
            selectedSlot = firstSlot(selectedDate);
            showStep(STEP_TICKET);
        }
    }

    private void showStep(int step) {
        currentStep = step;
        updateBackButtonVisibility();
        switch (step) {
            case STEP_DATE:   showDateStep();   break;
            case STEP_TIME:   showTimeStep();   break;
            case STEP_TICKET: showTicketStep(); break;
        }
    }

    private void goBack() {
        String type = scheduleType();
        if (currentStep == STEP_TICKET) {
            if ("multi_time".equals(type)) {
                showStep(STEP_TIME);
            } else if ("multi_day".equals(type)) {
                List<EventTimeSlot> slots = selectedDate != null ? selectedDate.getTimeSlots() : null;
                showStep((slots != null && slots.size() > 1) ? STEP_TIME : STEP_DATE);
            }
        } else if (currentStep == STEP_TIME) {
            showStep(STEP_DATE);
        }
    }

    private void updateBackButtonVisibility() {
        String type = scheduleType();
        boolean showBack = ("multi_time".equals(type) && currentStep == STEP_TICKET)
                || ("multi_day".equals(type) && (currentStep == STEP_TIME || currentStep == STEP_TICKET));
        binding.buttonBack.setVisibility(showBack ? View.VISIBLE : View.GONE);
    }

    // ── Step: Date ───────────────────────────────────────────────────────────

    private void showDateStep() {
        binding.textSheetTitle.setText(R.string.label_select_date);
        showSelectionList();

        List<EventDate> dates = event.getEventDates();
        if (dates == null) return;

        List<SelectionItem> items = new ArrayList<>();
        for (EventDate date : dates) {
            String primary = EventScheduleFormatter.formatDate(date.getDate(), true);
            List<EventTimeSlot> slots = date.getTimeSlots();
            String secondary = buildSlotCountLabel(slots);
            items.add(new SelectionItem(primary, secondary));
        }

        binding.recyclerSelection.setAdapter(new SelectionAdapter(items, index -> {
            selectedDate = dates.get(index);
            List<EventTimeSlot> slots = selectedDate.getTimeSlots();
            if (slots != null && slots.size() > 1) {
                showStep(STEP_TIME);
            } else {
                selectedSlot = (slots != null && !slots.isEmpty()) ? slots.get(0) : null;
                showStep(STEP_TICKET);
            }
        }));
    }

    private String buildSlotCountLabel(List<EventTimeSlot> slots) {
        if (slots == null || slots.isEmpty()) return null;
        if (slots.size() == 1) {
            return EventScheduleFormatter.formatTimeRange(
                    slots.get(0).getStartTime(), slots.get(0).getEndTime());
        }
        return slots.size() + " time slots";
    }

    // ── Step: Time ───────────────────────────────────────────────────────────

    private void showTimeStep() {
        binding.textSheetTitle.setText(R.string.label_select_time_slot);
        showSelectionList();

        if (selectedDate == null) return;
        List<EventTimeSlot> slots = selectedDate.getTimeSlots();
        if (slots == null) return;

        List<SelectionItem> items = new ArrayList<>();
        for (EventTimeSlot slot : slots) {
            items.add(new SelectionItem(
                    EventScheduleFormatter.formatTimeRange(slot.getStartTime(), slot.getEndTime()),
                    null));
        }

        binding.recyclerSelection.setAdapter(new SelectionAdapter(items, index -> {
            selectedSlot = slots.get(index);
            showStep(STEP_TICKET);
        }));
    }

    // ── Step: Ticket ─────────────────────────────────────────────────────────

    private void showTicketStep() {
        binding.textSheetTitle.setText(R.string.label_select_tickets);
        binding.recyclerSelection.setVisibility(View.GONE);
        binding.scrollTickets.setVisibility(View.VISIBLE);
        binding.layoutFooter.setVisibility(View.VISIBLE);

        List<EventTicket> tickets = selectedSlot != null ? selectedSlot.getTickets() : null;
        if (tickets == null) tickets = new ArrayList<>();

        ticketQuantities = new ArrayList<>();
        for (EventTicket t : tickets) {
            ticketQuantities.add(new TicketQuantity(t));
        }

        buildTicketRows();
        refreshTotal();
        binding.buttonConfirm.setOnClickListener(v -> onConfirmClicked());
    }

    private void buildTicketRows() {
        LinearLayout container = binding.layoutTicketRows;
        container.removeAllViews();

        for (int i = 0; i < ticketQuantities.size(); i++) {
            final int idx = i;
            TicketQuantity tq = ticketQuantities.get(i);

            ItemTicketRowBinding row = ItemTicketRowBinding.inflate(
                    LayoutInflater.from(requireContext()), container, false);

            row.textTicketName.setText(tq.ticket.getName());
            row.textTicketPrice.setText(formatTicketPrice(tq.ticket));
            row.textTicketAvailability.setText(
                    getString(R.string.label_available_qty, tq.ticket.getQty()));
            row.textQty.setText("0");
            row.buttonMinus.setEnabled(false);

            row.buttonMinus.setOnClickListener(v -> {
                TicketQuantity current = ticketQuantities.get(idx);
                if (current.qty > 0) {
                    current.qty--;
                    row.textQty.setText(String.valueOf(current.qty));
                    row.buttonMinus.setEnabled(current.qty > 0);
                    row.buttonPlus.setEnabled(current.qty < current.ticket.getQty());
                    refreshTotal();
                }
            });

            row.buttonPlus.setOnClickListener(v -> {
                TicketQuantity current = ticketQuantities.get(idx);
                if (current.qty < current.ticket.getQty()) {
                    current.qty++;
                    row.textQty.setText(String.valueOf(current.qty));
                    row.buttonMinus.setEnabled(current.qty > 0);
                    row.buttonPlus.setEnabled(current.qty < current.ticket.getQty());
                    refreshTotal();
                }
            });

            container.addView(row.getRoot());

            if (i < ticketQuantities.size() - 1) {
                addDivider(container);
            }
        }
    }

    private String formatTicketPrice(EventTicket ticket) {
        double price = ticket.getPriceAsDouble();
        if (price <= 0) return getString(R.string.label_free_ticket);
        return String.format(Locale.US, "LKR %,.0f", price);
    }

    private void refreshTotal() {
        if (ticketQuantities == null) return;
        double totalPaid = 0;
        boolean anySelected = false;
        for (TicketQuantity tq : ticketQuantities) {
            if (tq.qty > 0) {
                anySelected = true;
            }
            totalPaid += tq.ticket.getPriceAsDouble() * tq.qty;
        }
        if (!anySelected) {
            binding.textTotalPrice.setText(R.string.label_total_lkr_zero);
            return;
        }
        if (totalPaid <= 0) {
            binding.textTotalPrice.setText(R.string.label_free);
        } else {
            binding.textTotalPrice.setText(String.format(Locale.US, "LKR %,.0f", totalPaid));
        }
    }

    private void onConfirmClicked() {
        List<TicketQuantity> selectedTickets = new ArrayList<>();
        if (ticketQuantities != null) {
            for (TicketQuantity tq : ticketQuantities) {
                if (tq.qty > 0) {
                    selectedTickets.add(tq);
                }
            }
        }

        if (selectedTickets.isEmpty()) {
            Snackbar.make(binding.getRoot(), R.string.label_no_tickets_selected, Snackbar.LENGTH_SHORT).show();
            return;
        }

        try {
            JSONArray jsonArray = new JSONArray();
            for (TicketQuantity tq : selectedTickets) {
                JSONObject obj = new JSONObject();
                obj.put("ticketId", tq.ticket.getId());
                obj.put("ticketName", tq.ticket.getName());
                obj.put("ticketPrice", tq.ticket.getPrice());
                obj.put("qty", tq.qty);
                jsonArray.put(obj);
            }
            Bundle result = new Bundle();
            result.putString("ticketsJson", jsonArray.toString());
            
            // Add event data for checkout
            if (event != null) {
                result.putString("eventName", event.getName());
                result.putString("eventImageUrl", event.getImage());
            }
            
            // Add selected time slot data
            if (selectedSlot != null) {
                result.putString("eventDateTime", EventScheduleFormatter.formatDateTimeDisplay(
                        selectedDate != null ? selectedDate.getDate() : null,
                        null,
                        selectedSlot.getStartTime()));
                result.putString("eventVenue", event != null && event.getVenueName() != null ? event.getVenueName() : "");
            }
            
            getParentFragmentManager().setFragmentResult(RESULT_KEY, result);
            dismiss();
        } catch (Exception e) {
            Snackbar.make(binding.getRoot(), "Error preparing booking", Snackbar.LENGTH_LONG).show();
        }
    }

    // ── View helpers ─────────────────────────────────────────────────────────

    private void showSelectionList() {
        binding.recyclerSelection.setVisibility(View.VISIBLE);
        binding.scrollTickets.setVisibility(View.GONE);
        binding.layoutFooter.setVisibility(View.GONE);
    }

    private void addDivider(LinearLayout container) {
        View divider = new View(requireContext());
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 1);
        params.setMarginStart(dpToPx(16));
        params.setMarginEnd(dpToPx(16));
        divider.setLayoutParams(params);
        divider.setBackgroundColor(resolveAttrColor(com.google.android.material.R.attr.colorOutlineVariant));
        container.addView(divider);
    }

    private int resolveAttrColor(int attr) {
        android.util.TypedValue tv = new android.util.TypedValue();
        requireContext().getTheme().resolveAttribute(attr, tv, true);
        return tv.data;
    }

    private int dpToPx(int dp) {
        return Math.round(dp * getResources().getDisplayMetrics().density);
    }

    // ── Convenience accessors ─────────────────────────────────────────────────

    private String scheduleType() {
        return event.getScheduleType() != null ? event.getScheduleType() : "one_time";
    }

    @Nullable
    private EventDate firstDate() {
        List<EventDate> dates = event.getEventDates();
        return (dates != null && !dates.isEmpty()) ? dates.get(0) : null;
    }

    @Nullable
    private EventTimeSlot firstSlot(@Nullable EventDate date) {
        if (date == null) return null;
        List<EventTimeSlot> slots = date.getTimeSlots();
        return (slots != null && !slots.isEmpty()) ? slots.get(0) : null;
    }

    // ── Inner: data models ───────────────────────────────────────────────────

    private static final class TicketQuantity {
        final EventTicket ticket;
        int qty = 0;

        TicketQuantity(EventTicket ticket) {
            this.ticket = ticket;
        }
    }

    private static final class SelectionItem {
        final String primary;
        @Nullable final String secondary;

        SelectionItem(String primary, @Nullable String secondary) {
            this.primary = primary;
            this.secondary = secondary;
        }
    }

    // ── Inner: RecyclerView adapter ──────────────────────────────────────────

    private static final class SelectionAdapter extends RecyclerView.Adapter<SelectionAdapter.Vh> {

        interface OnItemClickListener {
            void onItemClick(int index);
        }

        private final List<SelectionItem> items;
        private final OnItemClickListener listener;

        SelectionAdapter(List<SelectionItem> items, OnItemClickListener listener) {
            this.items = items;
            this.listener = listener;
        }

        @NonNull
        @Override
        public Vh onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            ItemSelectionRowBinding b = ItemSelectionRowBinding.inflate(
                    LayoutInflater.from(parent.getContext()), parent, false);
            return new Vh(b);
        }

        @Override
        public void onBindViewHolder(@NonNull Vh holder, int position) {
            SelectionItem item = items.get(position);
            holder.binding.textPrimary.setText(item.primary);
            if (item.secondary != null) {
                holder.binding.textSecondary.setVisibility(View.VISIBLE);
                holder.binding.textSecondary.setText(item.secondary);
            } else {
                holder.binding.textSecondary.setVisibility(View.GONE);
            }
            holder.itemView.setOnClickListener(v -> listener.onItemClick(holder.getAdapterPosition()));
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        static final class Vh extends RecyclerView.ViewHolder {
            final ItemSelectionRowBinding binding;

            Vh(ItemSelectionRowBinding binding) {
                super(binding.getRoot());
                this.binding = binding;
            }
        }
    }
}
