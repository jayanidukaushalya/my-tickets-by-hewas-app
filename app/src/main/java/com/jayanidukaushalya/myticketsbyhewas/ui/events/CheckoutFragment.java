package com.jayanidukaushalya.myticketsbyhewas.ui.events;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.android.material.snackbar.Snackbar;
import com.google.gson.Gson;
import com.jayanidukaushalya.myticketsbyhewas.R;
import com.jayanidukaushalya.myticketsbyhewas.data.model.Customer;
import com.jayanidukaushalya.myticketsbyhewas.data.model.PurchaseResponse;
import com.jayanidukaushalya.myticketsbyhewas.data.model.ReservationResponse;
import com.jayanidukaushalya.myticketsbyhewas.data.remote.CustomerApiClient;
import com.jayanidukaushalya.myticketsbyhewas.data.remote.CustomerApiClientImpl;
import com.jayanidukaushalya.myticketsbyhewas.data.remote.PurchaseApiClient;
import com.jayanidukaushalya.myticketsbyhewas.data.remote.PurchaseApiClientImpl;
import com.jayanidukaushalya.myticketsbyhewas.data.model.ReserveTicketsRequest;
import com.jayanidukaushalya.myticketsbyhewas.databinding.FragmentCheckoutBinding;
import com.jayanidukaushalya.myticketsbyhewas.databinding.ItemCheckoutTicketRowBinding;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import lk.payhere.androidsdk.PHConfigs;
import lk.payhere.androidsdk.PHConstants;
import lk.payhere.androidsdk.PHMainActivity;
import lk.payhere.androidsdk.PHResponse;
import lk.payhere.androidsdk.model.InitRequest;

public class CheckoutFragment extends Fragment {

    private static final int PAYHERE_REQUEST_CODE = 456;

    private FragmentCheckoutBinding binding;
    private PurchaseApiClient purchaseApiClient;
    private CustomerApiClient customerApiClient;
    private final Gson gson = new Gson();

    /** Parsed list of all selected ticket types with quantities. */
    private final List<TicketItem> ticketItems = new ArrayList<>();

    private ReservationResponse pendingReservation;
    private boolean isProcessingPayment = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentCheckoutBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        purchaseApiClient = PurchaseApiClientImpl.getInstance();
        customerApiClient = CustomerApiClientImpl.getInstance();

        parseArgs();
        setupToolbar();
        buildTicketStub();
        populateContactInputs();
        setupValidationWatchers();
        binding.buttonPayNow.setOnClickListener(v -> onPayNowClicked());
    }

    private void setupValidationWatchers() {
        binding.inputEmail.addTextChangedListener(new android.text.TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                binding.layoutEmail.setError(null);
            }
            @Override public void afterTextChanged(android.text.Editable s) {}
        });
        binding.inputPhone.addTextChangedListener(new android.text.TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                binding.layoutPhone.setError(null);
            }
            @Override public void afterTextChanged(android.text.Editable s) {}
        });
    }

    // ── Arg parsing ─────────────────────────────────────────────────────────

    private void parseArgs() {
        Bundle args = getArguments();
        if (args == null) return;
        String json = args.getString("ticketsJson");
        if (json == null || json.isEmpty()) return;
        try {
            JSONArray arr = new JSONArray(json);
            for (int i = 0; i < arr.length(); i++) {
                JSONObject obj = arr.getJSONObject(i);
                TicketItem item = new TicketItem(
                        obj.optString("ticketId"),
                        obj.optString("ticketName"),
                        obj.optString("ticketPrice"),
                        obj.optInt("qty", 0)
                );
                if (!item.ticketId.isEmpty() && item.qty > 0) {
                    ticketItems.add(item);
                }
            }
        } catch (Exception e) {
            // ignore parse errors — caught later in buildTicketStub
        }
    }

    // ── Toolbar ──────────────────────────────────────────────────────────────

    private void setupToolbar() {
        binding.toolbar.setNavigationOnClickListener(v -> NavHostFragment.findNavController(this).navigateUp());
    }

    // ── Ticket stub UI ───────────────────────────────────────────────────────

    private void buildTicketStub() {
        if (ticketItems.isEmpty()) {
            Snackbar.make(binding.getRoot(), "Invalid checkout details", Snackbar.LENGTH_LONG).show();
            NavHostFragment.findNavController(this).navigateUp();
            return;
        }

        LinearLayout container = binding.layoutTicketItems;
        container.removeAllViews();

        double grandTotal = 0;

        for (int i = 0; i < ticketItems.size(); i++) {
            TicketItem item = ticketItems.get(i);
            double unitPrice = parsePrice(item.ticketPrice);
            double lineTotal = unitPrice * item.qty;
            grandTotal += lineTotal;

            ItemCheckoutTicketRowBinding row = ItemCheckoutTicketRowBinding.inflate(
                    LayoutInflater.from(requireContext()), container, false);

            // "2 X Economy Ticket"
            row.textItemQtyLabel.setText(item.qty + " × " + item.ticketName);
            // "Economy Ticket (LKR 50,000)"
            String priceFormatted = unitPrice > 0
                    ? String.format(Locale.US, "LKR %,.0f", unitPrice)
                    : getString(R.string.label_free_ticket);
            row.textItemName.setText(item.ticketName + " (" + priceFormatted + ")");
            // Line total
            String lineTotalText = lineTotal > 0
                    ? String.format(Locale.US, "LKR %,.0f", lineTotal)
                    : getString(R.string.label_free_ticket);
            row.textItemTotal.setText(lineTotalText);

            container.addView(row.getRoot());

            // Divider between rows
            if (i < ticketItems.size() - 1) {
                View divider = new View(requireContext());
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT, 1);
                divider.setLayoutParams(lp);
                divider.setBackgroundColor(0x1AFFFFFF); // outline_variant
                container.addView(divider);
            }
        }

        // Grand total in bottom bar
        String totalText = grandTotal > 0
                ? String.format(Locale.US, "LKR %,.0f", grandTotal)
                : getString(R.string.label_free_ticket);
        binding.textTotalPrice.setText(totalText);
        binding.buttonPayNow.setText(getString(R.string.action_pay_total, totalText));
    }

    // ── Contact fields ───────────────────────────────────────────────────────

    private void populateContactInputs() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            binding.inputEmail.setEnabled(true);
            binding.inputEmail.setText("");
            binding.inputPhone.setText("");
            return;
        }

        binding.inputEmail.setText(user.getEmail() != null ? user.getEmail() : "");
        binding.inputEmail.setEnabled(false);

        user.getIdToken(false).addOnSuccessListener(tokenResult -> {
            String token = tokenResult.getToken();
            if (token == null || token.isEmpty()) {
                binding.inputPhone.setText(user.getPhoneNumber() != null ? user.getPhoneNumber() : "");
                return;
            }
            customerApiClient.getCustomerByUid(token, new CustomerApiClient.CustomerCallback() {
                @Override
                public void onSuccess(Customer customer) {
                    String phone = customer.getPhone();
                    if (phone == null || phone.trim().isEmpty()) phone = user.getPhoneNumber();
                    binding.inputPhone.setText(phone != null ? phone : "");
                }
                @Override
                public void onFailure(String errorMessage) {
                    binding.inputPhone.setText(user.getPhoneNumber() != null ? user.getPhoneNumber() : "");
                }
            });
        }).addOnFailureListener(e -> binding.inputPhone.setText(user.getPhoneNumber() != null ? user.getPhoneNumber() : ""));
    }

    private void onPayNowClicked() {
        if (isProcessingPayment) return;
        clearErrors();

        String email = binding.inputEmail.getText() != null ? binding.inputEmail.getText().toString().trim() : "";
        String phone = binding.inputPhone.getText() != null ? binding.inputPhone.getText().toString().trim() : "";

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                binding.layoutEmail.setError(getString(R.string.error_invalid_email));
                return;
            }
        }
        if (phone.isEmpty()) {
            binding.layoutPhone.setError(getString(R.string.error_phone_required));
            return;
        }

        BuyerInfo buyerInfo = buildBuyerInfo(user, email, phone);
        reserveAllAndStartPayment(buyerInfo, user);
    }

    private void clearErrors() {
        binding.layoutEmail.setError(null);
        binding.layoutPhone.setError(null);
    }

    private BuyerInfo buildBuyerInfo(@Nullable FirebaseUser user, String emailInput, String phone) {
        if (user == null) return new BuyerInfo("Guest", "User", emailInput, phone);
        String firstName = "Guest", lastName = "User";
        if (user.getDisplayName() != null && !user.getDisplayName().trim().isEmpty()) {
            String[] parts = user.getDisplayName().trim().split("\\s+");
            firstName = parts[0];
            if (parts.length > 1) lastName = user.getDisplayName().trim().substring(firstName.length()).trim();
        }
        String email = user.getEmail() != null && !user.getEmail().isEmpty() ? user.getEmail() : emailInput;
        return new BuyerInfo(firstName, lastName, email, phone);
    }

    // ── Reservation: single bulk reserve call ───────────────────────────────

    private void reserveAllAndStartPayment(BuyerInfo buyerInfo, @Nullable FirebaseUser user) {
        setLoading(true);

        List<ReserveTicketsRequest.Item> items = new ArrayList<>();
        for (TicketItem item : ticketItems) {
            items.add(new ReserveTicketsRequest.Item(item.ticketId, item.qty));
        }

        if (user != null) {
            user.getIdToken(false)
                    .addOnSuccessListener(tokenResult ->
                            purchaseApiClient.reserveTicketsBulk(
                                    items,
                                    null,
                                    null,
                                    null,
                                    buyerInfo.phone,
                                    tokenResult.getToken(),
                                    new PurchaseApiClient.ReservationCallback() {
                                        @Override
                                        public void onSuccess(ReservationResponse reservation) {
                                            pendingReservation = reservation;
                                            startPayHerePayment(buyerInfo, reservation);
                                        }

                                        @Override
                                        public void onFailure(String errorMessage) {
                                            setLoading(false);
                                            Snackbar.make(binding.getRoot(), errorMessage, Snackbar.LENGTH_LONG).show();
                                        }
                                    }
                            )
                    )
                    .addOnFailureListener(e -> {
                        setLoading(false);
                        Snackbar.make(binding.getRoot(), "Authentication failed. Please sign in again.", Snackbar.LENGTH_LONG).show();
                    });
        } else {
            purchaseApiClient.reserveTicketsBulk(
                    items,
                    buyerInfo.email,
                    buyerInfo.firstName,
                    buyerInfo.lastName,
                    buyerInfo.phone,
                    null,
                    new PurchaseApiClient.ReservationCallback() {
                        @Override
                        public void onSuccess(ReservationResponse reservation) {
                            pendingReservation = reservation;
                            startPayHerePayment(buyerInfo, reservation);
                        }

                        @Override
                        public void onFailure(String errorMessage) {
                            setLoading(false);
                            Snackbar.make(binding.getRoot(), errorMessage, Snackbar.LENGTH_LONG).show();
                        }
                    }
            );
        }
    }

    // ── PayHere ───────────────────────────────────────────────────────────────

    private void startPayHerePayment(BuyerInfo buyerInfo, ReservationResponse reservation) {
        if (reservation == null || reservation.getPayment() == null) {
            setLoading(false);
            Snackbar.make(binding.getRoot(), "Payment configuration is missing", Snackbar.LENGTH_LONG).show();
            return;
        }

        ReservationResponse.PaymentInfo payment = reservation.getPayment();

        // Compute grand total across all reservations
        double grandTotal = 0;
        for (TicketItem item : ticketItems) {
            grandTotal += parsePrice(item.ticketPrice) * item.qty;
        }

        String sessionId = reservation.getSessionId();
        List<String> sessionIds = reservation.getSessionIds();

        InitRequest req = new InitRequest();
        req.setMerchantId(payment.getMerchantId());
        req.setMerchantSecret(payment.getMerchantSecret());
        req.setCurrency(payment.getCurrency());
        req.setAmount(grandTotal);
        req.setOrderId(payment.getOrderId() != null ? payment.getOrderId() : UUID.randomUUID().toString());
        req.setItemsDescription(buildItemsDescription());
        req.setNotifyUrl(payment.getNotifyUrl());
        if (sessionIds != null && !sessionIds.isEmpty()) {
            req.setCustom1(android.text.TextUtils.join(",", sessionIds));
        } else if (sessionId != null) {
            req.setCustom1(sessionId);
        }

        req.getCustomer().setFirstName(buyerInfo.firstName);
        req.getCustomer().setLastName(buyerInfo.lastName);
        req.getCustomer().setEmail(buyerInfo.email);
        req.getCustomer().setPhone(buyerInfo.phone);
        req.getCustomer().getAddress().setAddress("N/A");
        req.getCustomer().getAddress().setCity("Colombo");
        req.getCustomer().getAddress().setCountry("Sri Lanka");

        Intent intent = new Intent(requireActivity(), PHMainActivity.class);
        intent.putExtra(PHConstants.INTENT_EXTRA_DATA, req);
        PHConfigs.setBaseUrl(payment.isSandbox() ? PHConfigs.SANDBOX_URL : PHConfigs.LIVE_URL);
        startActivityForResult(intent, PAYHERE_REQUEST_CODE);
    }

    private String buildItemsDescription() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < ticketItems.size(); i++) {
            if (i > 0) sb.append(", ");
            sb.append(ticketItems.get(i).qty).append("x ").append(ticketItems.get(i).ticketName);
        }
        return sb.toString();
    }

    // ── PayHere result ────────────────────────────────────────────────────────

    @Override
    @SuppressWarnings("deprecation")
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode != PAYHERE_REQUEST_CODE) return;

        if (data == null || !data.hasExtra(PHConstants.INTENT_EXTRA_RESULT)) {
            setLoading(false);
            Snackbar.make(binding.getRoot(), "Payment cancelled", Snackbar.LENGTH_LONG).show();
            return;
        }

        PHResponse response = (PHResponse) data.getSerializableExtra(PHConstants.INTENT_EXTRA_RESULT);
        if (response == null) {
            setLoading(false);
            Snackbar.make(binding.getRoot(), "Payment cancelled", Snackbar.LENGTH_LONG).show();
            return;
        }

        String responseJson = gson.toJson(response).toUpperCase(Locale.US);
        boolean paymentSuccess = responseJson.contains("SUCCESS")
                || responseJson.contains("\"CODE\":1")
                || responseJson.contains("\"CODE\":2");

        if (!paymentSuccess) {
            setLoading(false);
            Snackbar.make(binding.getRoot(), "Payment was not completed", Snackbar.LENGTH_LONG).show();
            return;
        }

        if (pendingReservation == null) {
            setLoading(false);
            Snackbar.make(binding.getRoot(), "Purchase session is missing", Snackbar.LENGTH_LONG).show();
            return;
        }

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            user.getIdToken(false)
                    .addOnSuccessListener(tokenResult -> confirmAllPurchases(tokenResult.getToken()))
                    .addOnFailureListener(e -> { setLoading(false); Snackbar.make(binding.getRoot(), "Failed to confirm purchase", Snackbar.LENGTH_LONG).show(); });
        } else {
            confirmAllPurchases(null);
        }
    }

    private void confirmAllPurchases(@Nullable String token) {
        List<String> sessionIds = pendingReservation.getSessionIds();
        if (sessionIds == null || sessionIds.isEmpty()) {
            setLoading(false);
            Snackbar.make(binding.getRoot(), "Purchase session is missing", Snackbar.LENGTH_LONG).show();
            return;
        }

        purchaseApiClient.confirmPurchases(sessionIds, token, new PurchaseApiClient.PurchaseCallback() {
            @Override
            public void onSuccess(PurchaseResponse purchase) {
                onAllConfirmed();
            }

            @Override
            public void onFailure(String errorMessage) {
                setLoading(false);
                Snackbar.make(binding.getRoot(), errorMessage, Snackbar.LENGTH_LONG).show();
            }
        });
    }

    private void onAllConfirmed() {
        setLoading(false);
        Snackbar.make(binding.getRoot(), "Payment successful! Tickets purchased.", Snackbar.LENGTH_LONG).show();
        NavHostFragment.findNavController(CheckoutFragment.this).navigateUp();
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private void setLoading(boolean loading) {
        isProcessingPayment = loading;
        binding.buttonPayNow.setEnabled(!loading);
        if (loading) {
            binding.buttonPayNow.setText("Processing…");
        } else {
            buildTicketStub(); // restore button text
        }
    }

    private double parsePrice(@Nullable String price) {
        if (price == null) return 0;
        try { return Double.parseDouble(price); } catch (NumberFormatException e) { return 0; }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    // ── Data classes ──────────────────────────────────────────────────────────

    private static final class TicketItem {
        final String ticketId;
        final String ticketName;
        final String ticketPrice;
        final int qty;
        TicketItem(String ticketId, String ticketName, String ticketPrice, int qty) {
            this.ticketId = ticketId;
            this.ticketName = ticketName;
            this.ticketPrice = ticketPrice;
            this.qty = qty;
        }
    }

    private static final class BuyerInfo {
        final String firstName, lastName, email, phone;
        BuyerInfo(String firstName, String lastName, String email, String phone) {
            this.firstName = firstName; this.lastName = lastName;
            this.email = email; this.phone = phone;
        }
    }
}
