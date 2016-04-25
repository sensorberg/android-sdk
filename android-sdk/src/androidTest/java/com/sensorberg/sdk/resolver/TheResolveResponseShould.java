package com.sensorberg.sdk.resolver;

import com.google.gson.Gson;

import com.sensorberg.sdk.Constants;
import com.sensorberg.sdk.SensorbergTestApplication;
import com.sensorberg.sdk.di.TestComponent;
import com.sensorberg.sdk.model.server.ResolveResponse;
import com.sensorberg.sdk.scanner.ScanEvent;
import com.sensorberg.sdk.scanner.ScanEventType;

import org.fest.assertions.api.Assertions;
import org.junit.Test;

import android.test.AndroidTestCase;

import java.util.Calendar;
import java.util.Date;

import javax.inject.Inject;

import util.TestConstants;
import util.Utils;

public class TheResolveResponseShould extends AndroidTestCase  {

    @Inject
    Gson gson;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        ((TestComponent) SensorbergTestApplication.getComponent()).inject(this);
    }

    static final ScanEvent RESOLVABLE_ENTRY_EVENT_WITH_ID_1 = new ScanEvent.Builder()
        .withBeaconId(TestConstants.LEET_BEACON_ID_1)
        .withEventMask(ScanEventType.ENTRY.getMask())
        .build();

    static final ScanEvent RESOLVABLE_EXIT_EVENT_WITH_ID_4 = new ScanEvent.Builder()
            .withBeaconId(TestConstants.LEET_BEACON_ID_4)
            .withEventMask(ScanEventType.EXIT.getMask())
            .build();

    static final ScanEvent NON_RESOLVABLE_ENTRY_EVENT_WITH_ID_4 = new ScanEvent.Builder()
            .withBeaconId(TestConstants.LEET_BEACON_ID_4)
            .withEventMask(ScanEventType.ENTRY.getMask())
            .build();
    private static final int OCLOCK = 1;

    @Test
    public void parse_a_regular_resolve_action() throws Exception {
        ResolveResponse tested = gson.fromJson(Utils.getRawResourceAsString(com.sensorberg.sdk.test.R.raw.resolve_response_001, getContext()), ResolveResponse.class);

        Assertions.assertThat(tested.resolve(RESOLVABLE_ENTRY_EVENT_WITH_ID_1, 0)).hasSize(2+1);
        Assertions.assertThat(tested.getInstantActions()).hasSize(1);        
    }

    @Test
    public void resolve_an_exit_action() throws Exception {
        ResolveResponse tested = gson.fromJson(Utils.getRawResourceAsString(com.sensorberg.sdk.test.R.raw.resolve_response_001, getContext()), ResolveResponse.class);

        Assertions.assertThat(tested.resolve(RESOLVABLE_EXIT_EVENT_WITH_ID_4, 0)).hasSize(1+1);
        Assertions.assertThat(tested.getInstantActions()).hasSize(1);
    }

    @Test
    public void not_resolve_an_entry_action() throws Exception {
        ResolveResponse tested = gson.fromJson(Utils.getRawResourceAsString(com.sensorberg.sdk.test.R.raw.resolve_response_001, getContext()), ResolveResponse.class);

        Assertions.assertThat(tested.resolve(NON_RESOLVABLE_ENTRY_EVENT_WITH_ID_4, 0)).hasSize(1);
        Assertions.assertThat(tested.getInstantActions()).hasSize(1);
    }

    @Test
    public void not_have_instantActions_if_none_in_response() throws Exception {
        ResolveResponse tested = gson.fromJson(Utils.getRawResourceAsString(com.sensorberg.sdk.test.R.raw.resolve_response_002, getContext()), ResolveResponse.class);

        Assertions.assertThat(tested.resolve(RESOLVABLE_ENTRY_EVENT_WITH_ID_1, 0)).hasSize(1);
        Assertions.assertThat(tested.getInstantActions()).hasSize(0);
    }

    @Test
    public void not_have_absolutely_no_actions() throws Exception {
        ResolveResponse tested = gson.fromJson(Utils.getRawResourceAsString(com.sensorberg.sdk.test.R.raw.resolve_response_002, getContext()), ResolveResponse.class);

        Assertions.assertThat(tested.resolve(NON_RESOLVABLE_ENTRY_EVENT_WITH_ID_4, 0)).hasSize(0);
        Assertions.assertThat(tested.getInstantActions()).hasSize(0);
    }

    @Test
    public void have_instantActions() throws Exception {
        ResolveResponse tested = gson.fromJson(Utils.getRawResourceAsString(com.sensorberg.sdk.test.R.raw.resolve_response_003, getContext()), ResolveResponse.class);

        Assertions.assertThat(tested.resolve(RESOLVABLE_ENTRY_EVENT_WITH_ID_1, 0)).hasSize(2);
        Assertions.assertThat(tested.getInstantActions()).hasSize(1);
    }

    @Test
    public void respect_the_timeFrames() throws Exception {
        ResolveResponse tested = gson.fromJson(Utils.getRawResourceAsString(com.sensorberg.sdk.test.R.raw.resolve_response_005, getContext()), ResolveResponse.class);

        Assertions.assertThat(tested.resolve(RESOLVABLE_ENTRY_EVENT_WITH_ID_1, new Date(Constants.Time.ONE_HOUR * 12).getTime())).hasSize(1);
        Assertions.assertThat(tested.resolve(RESOLVABLE_ENTRY_EVENT_WITH_ID_1, newDate(1968, Calendar.JANUARY, 0, 0).getTime())).hasSize(1);
        Assertions.assertThat(tested.resolve(RESOLVABLE_ENTRY_EVENT_WITH_ID_1, newDate(1971, Calendar.FEBRUARY, 0, 0).getTime())).hasSize(1);

        Assertions.assertThat(tested.resolve(RESOLVABLE_ENTRY_EVENT_WITH_ID_1, newDate(1970, Calendar.MARCH, 0, 0).getTime())).hasSize(0);

    }

    private Date newDate(int year, int month, int dayOfMonth, int hour) {
        Calendar calendar = Calendar.getInstance();
        calendar.clear();
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(year, month, dayOfMonth);
        return calendar.getTime();
    }


}
