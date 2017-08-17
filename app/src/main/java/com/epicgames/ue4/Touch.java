package com.epicgames.ue4;

final class Touch {
    final float x;
    final float y;
    /**
     * 0 = new touch
     * 1 = hold
     * 2 = released
     */
    final byte state;
    final long timestamp;

    Touch(final float x, final float y, final byte state) {
        this.x = x;
        this.y = y;
        this.state = state;
        this.timestamp = System.currentTimeMillis();
    }

    Touch(final float x, final float y, final byte state, final long timestamp) {
        this.x = x;
        this.y = y;
        this.state = state;
        this.timestamp = timestamp;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }

        final Touch touch = (Touch) o;

        return Float.compare(touch.x, this.x) == 0 && Float.compare(touch.y, this.y) == 0 && this.state == touch.state;

    }

    @Override
    public int hashCode() {
        int result = this.x == +0.0f ? 0 : Float.floatToIntBits(this.x);
        result = 31 * result + (this.y == +0.0f ? 0 : Float.floatToIntBits(this.y));
        result = 31 * result + this.state;
        return result;
    }
}
