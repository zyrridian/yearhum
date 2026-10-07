package com.yearhum.app.core.designsystem.component

import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import com.yearhum.app.R
import java.time.Year

const val MIN_BIRTH_YEAR = 1900

fun parseBirthYear(text: String, maxYear: Int = Year.now().value): Int? = text.trim().toIntOrNull()?.takeIf { it in MIN_BIRTH_YEAR..maxYear }

/** Asks for a birth year. [onDismiss] is "skip"/"cancel"; the caller decides what that means. */
@Composable
fun BirthYearDialog(
    initialYear: Int?,
    onConfirm: (Int) -> Unit,
    onDismiss: () -> Unit,
    dismissLabel: String = stringResource(R.string.birth_year_skip),
) {
    var text by rememberSaveable { mutableStateOf(initialYear?.toString().orEmpty()) }
    val parsed = parseBirthYear(text)
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.birth_year_title)) },
        text = {
            OutlinedTextField(
                value = text,
                onValueChange = { text = it.filter(Char::isDigit).take(4) },
                label = { Text(stringResource(R.string.birth_year_label)) },
                supportingText = {
                    Text(
                        stringResource(
                            R.string.birth_year_hint,
                            MIN_BIRTH_YEAR,
                            Year.now().value,
                        ),
                    )
                },
                isError = text.isNotEmpty() && parsed == null,
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            )
        },
        confirmButton = {
            TextButton(onClick = { parsed?.let(onConfirm) }, enabled = parsed != null) {
                Text(stringResource(R.string.birth_year_save))
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(dismissLabel) } },
    )
}
