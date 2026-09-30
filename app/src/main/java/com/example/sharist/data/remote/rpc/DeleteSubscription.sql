create or replace function delete_subscription(
    p_subscription_id uuid
)
returns text
language plpgsql
as $$
declare
    v_departure timestamp;
    v_cancel_days int;
    v_limit timestamp;
    v_ride_id uuid;
begin

    -- Get ride info + ride_id
    select r."departureDateTime",
           r."cancelUpToDays",
           s."rideId"
    into v_departure, v_cancel_days, v_ride_id
    from subscription s
    join ride r on r.id = s."rideId"
    where s.id = p_subscription_id;

    -- Subscription not found
    if v_departure is null then
        return 'NOT_FOUND';
    end if;

    -- Calculate cancellation deadline
    v_limit := v_departure - (v_cancel_days * interval '1 day');

    -- Check rule
    if now() > v_limit then
        return 'TOO_LATE';
    end if;

    -- Delete subscription
    delete from subscription
    where id = p_subscription_id;

    -- 🔥 RESTORE SEAT
    update ride
    set "availableSeats" = "availableSeats" + 1
    where id = v_ride_id;

    return 'OK';

end;
$$;