package com.example.nesh.mcc_calendar;

import net.fortuna.ical4j.model.Date;

import java.text.ParseException;

/**
 * Created by Alberto Vaccari on 23-Oct-15.
 */

public class Event {

    private String _id, summary, description, location, visibility, freq, weekStart;
    private int interval;
    private Date dateStart, dateEnd, until;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Event event = (Event) o;

        return _id.equals(event._id);

    }

    @Override
    public int hashCode() {
        return _id.hashCode();
    }

    public Event(String _id, String summary, String description, String location, String visibility, String freq, String weekStart, String dateStart, String dateEnd, String until, String interval) throws ParseException {
        this(_id, summary, description, location, visibility, freq, weekStart, new Date(dateStart), new Date(dateEnd), new Date(until), Integer.parseInt(interval));
    }

    public Event(String _id, String summary, String description, String location, String visibility, String freq, String weekStart, String dateStart, String dateEnd, String until, int interval) throws ParseException {
        this(_id, summary, description, location, visibility, freq, weekStart, new Date(dateStart), new Date(dateEnd), new Date(until), interval);
    }

    public Event(String _id, String summary, String description, String location, String visibility, String freq, String weekStart, Date dateStart, Date dateEnd, Date until, int interval){
        this._id = _id;
        this.dateStart = dateStart;
        this.summary = summary;
        this.description = description;
        this.location = location;
        this.dateEnd = dateEnd;
        this.freq = freq;
        this.weekStart = weekStart;
        this.until = until;
        this.interval = interval;
        this.visibility = visibility;
    }

    @Override
    public String toString() {
        return "Event{" +
                "_id='" + _id + '\'' +
                ", summary='" + summary + '\'' +
                ", description='" + description + '\'' +
                ", location='" + location + '\'' +
                ", visibility='" + visibility + '\'' +
                ", freq='" + freq + '\'' +
                ", weekStart='" + weekStart + '\'' +
                ", interval=" + interval +
                ", dateStart=" + dateStart +
                ", dateEnd=" + dateEnd +
                ", until=" + until +
                '}';
    }

    public void set_id(String _id) {
        this._id = _id;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public void setVisibility(String visibility) {
        this.visibility = visibility;
    }

    public void setFreq(String freq) {
        this.freq = freq;
    }

    public void setWeekStart(String weekStart) {
        this.weekStart = weekStart;
    }

    public void setInterval(int interval) {
        this.interval = interval;
    }

    public void setDateStart(Date dateStart) {
        this.dateStart = dateStart;
    }

    public void setDateEnd(Date dateEnd) {
        this.dateEnd = dateEnd;
    }

    public void setUntil(Date until) {
        this.until = until;
    }

    public String get_id() {

        return _id;
    }

    public String getSummary() {
        return summary;
    }

    public String getDescription() {
        return description;
    }

    public String getLocation() {
        return location;
    }

    public String getVisibility() {
        return visibility;
    }

    public String getFreq() {
        return freq;
    }

    public String getWeekStart() {
        return weekStart;
    }

    public int getInterval() {
        return interval;
    }

    public Date getDateStart() {
        return dateStart;
    }

    public Date getDateEnd() {
        return dateEnd;
    }

    public Date getUntil() {
        return until;
    }
}
