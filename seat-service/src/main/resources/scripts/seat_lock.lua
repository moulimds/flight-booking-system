-- Atomic Redis Seat Lock Script
-- KEYS[1] = LOCK:FLIGHT_{flightId}:SEAT_{seatNumber} (e.g. LOCK:FLIGHT_102:SEAT_14A)
-- ARGV[1] = ownerId / userId (e.g. 1)
-- ARGV[2] = ttlSeconds (e.g. 600 for 10 minutes, or 300 for 5 minutes)
if redis.call('exists', KEYS[1]) == 0 then
    redis.call('set', KEYS[1], ARGV[1], 'EX', ARGV[2])
    return 1
else
    return 0
end
