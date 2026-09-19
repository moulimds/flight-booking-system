-- Atomic Redis Seat Unlock Script
-- KEYS[1] = LOCK:FLIGHT_{flightId}:SEAT_{seatNumber}
-- ARGV[1] = ownerId / userId
if redis.call('get', KEYS[1]) == ARGV[1] then
    return redis.call('del', KEYS[1])
else
    return 0
end
