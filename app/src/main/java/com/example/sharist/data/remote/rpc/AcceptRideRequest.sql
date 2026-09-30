create or replace function accept_ride_request(
    p_request_id uuid,
    p_driver_id uuid
)
returns text
language plpgsql
as $$
begin

    update "rideRequest"
    set "driverId" = p_driver_id
    where id = p_request_id
      and "driverId" is null;

    if not found then
        if exists (select 1 from "rideRequest" where id = p_request_id) then
            return 'ALREADY_HAS_DRIVER';
        else
            return 'NOT_FOUND';
        end if;
    end if;

    return 'OK';

end;
$$;