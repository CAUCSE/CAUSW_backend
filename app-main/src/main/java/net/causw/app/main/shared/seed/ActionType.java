package net.causw.app.main.shared.seed;

public enum ActionType {
    // Whale(70%), Dolphin(25%), Minnow(5%)
    POST(0.70, 0.70 + 0.25, 3.0),

    // Whale(40%), Dolphin(40%), Minnow(20%)
    COMMENT(0.40, 0.40 + 0.40, 2.0),

    // Whale(20%), Dolphin(30%), Minnow(50%)
    LIKE(0.20, 0.20 + 0.30, 1.0);

    // 누적 임계값 (0.0 ~ 1.0)
    public final double whaleThreshold;   // 이 값보다 작으면 Whale
    public final double dolphinThreshold; // 이 값보다 작으면 Dolphin, 아니면 Minnow

    // 그룹 내 편중도 (Zipfian exponent)
    // 행동이 어려울수록(Post) 상위 유저 중에서도 더 상위에게 몰리는 경향(3.0)이 심함
    // 행동이 쉬울수록(Like) 그룹 내에서는 평등하게 퍼짐(1.0)
    public final double skewFactor;

    ActionType(double whaleProb, double cumulativeDolphinProb, double skewFactor) {
        this.whaleThreshold = whaleProb;
        this.dolphinThreshold = cumulativeDolphinProb;
        this.skewFactor = skewFactor;
    }
}
