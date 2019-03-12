package com.jiyong.spark;

import org.apache.commons.lang3.StringUtils;

import java.net.InetAddress;

public class SnowFlake {
    private static final long START_STMP = 1480166465631L;
    private static final long SEQUENCE_BIT = 12L;
    private static final long MACHINE_BIT = 10L;
    private static final long DATACENTER_BIT = 0L;
    private static final long MAX_DATACENTER_NUM = 0L;
    private static final long MAX_MACHINE_NUM = 1023L;
    private static final long MAX_SEQUENCE = 4095L;
    private static final long MACHINE_LEFT = 12L;
    private static final long DATACENTER_LEFT = 22L;
    private static final long TIMESTMP_LEFT = 22L;
    private long datacenterId;
    private long machineId;
    private long sequence = 0L;
    private long lastStmp = -1L;
    private static SnowFlake instance;

    public static SnowFlake getInstance() {
        return instance == null ? (instance = new SnowFlake(0L, 0L)) : instance;
    }

    public SnowFlake(long datacenterId, long machineId) {
        machineId = (long)getMachineId();
        if (machineId <= 1023L && machineId >= 0L) {
            this.datacenterId = datacenterId;
            this.machineId = machineId;
        } else {
            throw new IllegalArgumentException("machineId can't be greater than MAX_MACHINE_NUM or less than 0");
        }
    }

    public synchronized long nextId() {
        long currStmp = this.getNewstmp();
        if (currStmp < this.lastStmp) {
            throw new RuntimeException("Clock moved backwards.  Refusing to generate id");
        } else {
            if (currStmp == this.lastStmp) {
                this.sequence = this.sequence + 1L & 4095L;
                if (this.sequence == 0L) {
                    currStmp = this.getNextMill();
                }
            } else {
                this.sequence = 0L;
            }

            this.lastStmp = currStmp;
            return currStmp - 1480166465631L << 22 | this.machineId << 12 | this.sequence;
        }
    }

    private long getNextMill() {
        long mill;
        for(mill = this.getNewstmp(); mill <= this.lastStmp; mill = this.getNewstmp()) {
            ;
        }

        return mill;
    }

    private long getNewstmp() {
        return System.currentTimeMillis();
    }

    private static int getMachineId() {
        try {
            String dockerId = System.getProperty("DOCKERID");
            if (StringUtils.isBlank(dockerId)) {
                String[] ips = InetAddress.getLocalHost().getHostAddress().split("\\.");
                return Integer.valueOf(ips[2]) + 256 + Integer.valueOf(ips[3]);
            } else {
                return Integer.valueOf(dockerId);
            }
        } catch (Exception var2) {
            throw new IllegalArgumentException("get machineId error" + var2.toString());
        }
    }
}
