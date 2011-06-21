package com.proserus.stocks.model.transactions;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;

import org.joda.time.DateTime;

import com.proserus.stocks.model.common.PersistentModel;
import com.proserus.stocks.model.symbols.Symbol;
import com.proserus.stocks.utils.BigDecimalUtils;


@Entity
@NamedQueries( {
        @NamedQuery(name = "transaction.findAll", query = "SELECT t FROM Transaction t"),
        @NamedQuery(name = "transaction.findAllBySymbol", query = "SELECT t FROM Transaction t WHERE symbol_id = :symbolId"),
        @NamedQuery(name = "transaction.findAllByCurrency", query = "SELECT t FROM Transaction t, Symbol s WHERE symbol_id = s.id AND s.currency = :currency"),
        @NamedQuery(name = "transaction.findAllByLabel", query = "SELECT t FROM Transaction t WHERE :label in elements(t.labels)"),
        @NamedQuery(name = "transaction.findMinDate", query = "SELECT min(date) FROM Transaction t")
        })
public class Transaction extends PersistentModel{
	public static String IN_LABELS = "in elements(t.labels)";

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE)
	private Integer id;

	public Integer getId() {
		return id;
	}

	private static final String YYYY_M_MDD = "yyyyMMdd";
	private static final String SEMICOLON_STR = ";";

	public Transaction() {
		// for JPA
	}

	@ManyToMany( 
			targetEntity=Label.class, 
			cascade={CascadeType.PERSIST,CascadeType.MERGE})
	@JoinTable(
			name = "TRANSACTION_LABEL", 
			joinColumns =@JoinColumn(name = "transactionId"),
			inverseJoinColumns = @JoinColumn(name = "labelId")
		)
	private Collection<Label> labels = new ArrayList<Label>();

	@Column(nullable = false, columnDefinition="DECIMAL(38,8)")
	//Add constraint for min 0
	private BigDecimal quantity;

	@Column(nullable = false)
	private TransactionType type;

	@Column(nullable = false, columnDefinition="DECIMAL(38,8)")
	//Add constraint for min 0
	private BigDecimal commission;

	@Column(nullable = false, columnDefinition="DECIMAL(38,8)")
	//Add constraint for min 0
	private BigDecimal price;

	@ManyToOne(cascade = CascadeType.ALL, optional = false)
	//TODO Symbol ? @Column(nullable = false)
	private Symbol symbol;

	
	public Symbol getSymbol() {
		return symbol;
	}

	//Add constraint to check that the date is before Today (sysdate)
	private Date date;

	public void setDate(Date date) {
		this.date = date;
	}
	
	public void setDateTime(DateTime date) {
		this.date = date.toDate();
	}
	
	public DateTime getDateTime() {
		return new DateTime(date);
	}

	public Date getDate() {
		return date;
	}

	public TransactionType getType() {
		return type;
	}

	public BigDecimal getPrice() {
		return price;
	}

	public BigDecimal getQuantity() {
		return quantity;
	}

	public BigDecimal getCommission() {
		return commission;
	}

	//TODO Maybe the same label can be set twice
	//When removing labels.. we need to remove the transaction link too...
	public void setLabels(Collection<Label> labels) {
		if (labels == null || labels.contains(null)) {
			throw new NullPointerException();
		}

		if (labels.contains("")){
			throw new IllegalArgumentException();
		}

		for (Label label : this.labels) {
			label.removeTransaction(this);
		}
		this.labels.clear();
		for (Label label : labels) {
			addLabel(label);
		}
	}

	//TODO Maybe the same label can be set twice 
	public void addLabel(Label label) {
		if (label == null) {
			throw new NullPointerException();
		}

		if (label.getName().isEmpty()) {
			throw new IllegalArgumentException();
		}

		this.labels.add(label);
		label.addTransaction(this);
	}

	public void setType(TransactionType type) {
		if (type == null) {
			throw new NullPointerException();
		}
		this.type = type;
	}

	public void removeLabel(Label label) {
		if(label == null){
			throw new NullPointerException();
		}
		
		if(label.getName().isEmpty()){
			throw new IllegalArgumentException();
		}
		
		labels.remove(label);
		label.removeTransaction(this);
	}

	public void setSymbol(Symbol symbol) {
		if (symbol == null) {
			throw new NullPointerException();
		}

		this.symbol = symbol;
	}

	public void setPrice(BigDecimal price) {
		if (price == null) {
			throw new NullPointerException();
		}
		this.price = BigDecimalUtils.setDecimalWithScale(price);
	}

	public void setQuantity(BigDecimal quantity) {
		if (quantity == null) {
			throw new NullPointerException();
		}
		this.quantity = BigDecimalUtils.setDecimalWithScale(quantity);
	}

	public void setCommission(BigDecimal commission) {
		if (commission == null) {
			throw new NullPointerException();
		}
		
		this.commission = BigDecimalUtils.setDecimalWithScale(commission);
	}

	public Collection<Label> getLabelsValues() {
		return labels;
	}

	@Override
	public String toString() {
		SimpleDateFormat sdf = new SimpleDateFormat(YYYY_M_MDD);
		return sdf.format(date) + SEMICOLON_STR + getType() + SEMICOLON_STR + getSymbol().getTicker() + SEMICOLON_STR
		        + getPrice() + SEMICOLON_STR + getQuantity() + SEMICOLON_STR + getCommission() + SEMICOLON_STR
		        + labels.toString() + SEMICOLON_STR;
	}
}
