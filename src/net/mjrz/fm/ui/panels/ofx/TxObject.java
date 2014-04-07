package net.mjrz.fm.ui.panels.ofx;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.math.BigDecimal;
import java.util.Date;

import net.mjrz.fm.entity.beans.Account;
import net.mjrz.fm.utils.indexer.IndexedEntity;

public class TxObject {
	public static enum TxType {
		CREDIT, DEBIT;
	}

	private TxType type;
	private Account source;
	private IndexedEntity match;
	private BigDecimal amount;
	private Date date;
	private String notes;
	private String markup;
	private String fitId;
	private boolean doImport;
	private String ofxLine;
	private String importStatusMessage;

	private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);

	public TxType getType() {
		return type;
	}

	public void setType(TxType type) {
		this.type = type;
	}

	public Account getSource() {
		return source;
	}

	public void setSource(Account source) {
		this.source = source;
	}

	public IndexedEntity getMatch() {
		return match;
	}

	public void setMatch(IndexedEntity match) {
		this.match = match;
		pcs.firePropertyChange("match", null, match);
	}

	public BigDecimal getAmount() {
		return amount;
	}

	public void setAmount(BigDecimal amount) {
		this.amount = amount;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public String getNotes() {
		return notes;
	}

	public void setNotes(String notes) {
		this.notes = notes;
		pcs.firePropertyChange("notes", null, notes);
	}

	public String getMarkup() {
		return markup;
	}

	public void setMarkup(String markup) {
		this.markup = markup;
		pcs.firePropertyChange("markup", null, markup);
	}

	public boolean isDoImport() {
		return doImport;
	}

	public void setDoImport(boolean doImport) {
		this.doImport = doImport;
	}

	public String getFitId() {
		return fitId;
	}

	public void setFitId(String fitId) {
		this.fitId = fitId;
	}

	public String getOfxLine() {
		return ofxLine;
	}

	public void setOfxLine(String ofxLine) {
		this.ofxLine = ofxLine;
	}

	public String getImportStatusMessage() {
		return importStatusMessage;
	}

	public void setImportStatusMessage(String importStatusMessage) {
		this.importStatusMessage = importStatusMessage;
	}

	public void addPropertyChangeListener(PropertyChangeListener listener) {
		this.pcs.addPropertyChangeListener(listener);
	}

	public void removePropertyChangeListener(PropertyChangeListener listener) {
		this.pcs.removePropertyChangeListener(listener);
	}
}
