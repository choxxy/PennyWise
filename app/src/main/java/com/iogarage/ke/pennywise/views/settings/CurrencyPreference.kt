package com.iogarage.ke.pennywise.views.settings

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.SharedPreferences
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.AdapterView
import android.widget.EditText
import android.widget.ListView
import androidx.preference.ListPreference
import androidx.preference.PreferenceManager
import com.iogarage.ke.pennywise.R
import java.util.Locale

class CurrencyPreference(context: Context, attrs: AttributeSet?) : ListPreference(context, attrs) {
    private var searchEditText: EditText? = null
    private var currencyListView: ListView? = null
    private var alertDialog: AlertDialog? = null
    private var currencyName: Array<CharSequence> = emptyArray()
    private var currencyCode: Array<CharSequence> = emptyArray()
    private var adapter: CurrencyListAdapter? = null
    private val currenciesList: MutableList<ExtendedCurrency> = ArrayList()
    private var selectedCurrenciesList: MutableList<ExtendedCurrency> = ArrayList()
    private var defaultCurrencyCode: String? = null
    private var preferences: SharedPreferences
    private var editor: SharedPreferences.Editor

    init {
        preferences = PreferenceManager.getDefaultSharedPreferences(context)
        setCurrenciesList(ExtendedCurrency.getAllCurrencies())
        editor = preferences.edit()
        val key = preferences.getString(key, "USD")
        val symbol = preferences.getString(CURRENCY_SYMBOL, "$")
        val name = preferences.getString(CURRENCY_NAME, "United States Dollar")
        summary = "$name, $symbol"
        val a = context.theme.obtainStyledAttributes(attrs, R.styleable.attrs_currency, 0, 0)
        defaultCurrencyCode = try {
            a.getString(R.styleable.attrs_currency_currencyCode)
        } finally {
            a.recycle()
        }
    }

    override fun onClick() {
        val builder = AlertDialog.Builder(context)
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view = inflater.inflate(R.layout.currency_picker, null)
        searchEditText = view.findViewById(R.id.currency_code_picker_search)
        currencyListView = view.findViewById(R.id.currency_code_picker_listview)
        searchEditText?.addTextChangedListener(object : TextWatcher {
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun afterTextChanged(s: Editable) {
                search(s.toString())
            }
        })
        val currencyListView = view.findViewById<ListView>(R.id.currency_code_picker_listview)
        selectedCurrenciesList = ArrayList(currenciesList.size)
        selectedCurrenciesList.addAll(currenciesList)
        adapter = CurrencyListAdapter(context, selectedCurrenciesList)
        currencyListView.adapter = adapter
        currencyListView.onItemClickListener =
            AdapterView.OnItemClickListener { _, _, position, _ ->
                val currency = selectedCurrenciesList[position]
                value = currency.symbol
                summary = "${currency.name}, ${currency.symbol}"
                editor.putString(key, currency.code)
                editor.putString(CURRENCY_SYMBOL, currency.symbol)
                editor.putString(CURRENCY_NAME, currency.name)
                editor.commit()
                alertDialog?.dismiss()
            }
        builder.setView(view)
        builder.setNegativeButton("Cancel", null)
        builder.setPositiveButton(null, null)
        currencyCode = entries
        currencyName = entryValues
        check(currencyCode.size == currencyName.size)
        { "Preference requires an entries array and an entryValues array which are both the same length" }
        alertDialog = builder.create()
        alertDialog?.show()
    }

    @SuppressLint("DefaultLocale")
    private fun search(text: String) {
        selectedCurrenciesList.clear()
        for (currency in currenciesList) {
            if (currency.name.lowercase().contains(text.lowercase(Locale.getDefault()))) {
                selectedCurrenciesList.add(currency)
            }
        }
        adapter!!.notifyDataSetChanged()
    }

    private fun setCurrenciesList(newCurrencies: List<ExtendedCurrency>?) {
        currenciesList.clear()
        currenciesList.addAll(newCurrencies!!)
        entries = newCurrencies.map { it.name }.toTypedArray()
        entryValues = newCurrencies.map { it.symbol }.toTypedArray()
    }


    companion object {
        const val CURRENCY_SYMBOL = "currency_symbol"
        const val CURRENCY_NAME = "currency_name"
    }
}