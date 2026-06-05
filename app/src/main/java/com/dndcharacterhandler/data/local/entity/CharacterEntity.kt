package com.dndcharacterhandler.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Relation
import androidx.room.Embedded
import com.dndcharacterhandler.domain.model.InventoryCategory

@Entity(tableName = "characters")
data class CharacterEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val race: String,
    val characterClass: String,
    val subclass: String,
    val level: Int,
    val portraitUri: String?,
    val currentHp: Int,
    val maxHp: Int,
    val armorClass: Int,
    val speed: Int,
    val initiative: Int,
    val experience: Int,
    val strength: Int,
    val dexterity: Int,
    val constitution: Int,
    val intelligence: Int,
    val wisdom: Int,
    val charisma: Int,
    val alignment: String,
    val background: String,
    val faith: String,
    val homeland: String,
    val age: String,
    val gender: String,
    val height: String,
    val weight: String,
    val eyes: String,
    val hair: String,
    val skin: String,
    val personalityTraits: String,
    val ideals: String,
    val bonds: String,
    val flaws: String,
    val biography: String,
    val createdAt: Long,
    val updatedAt: Long
)

@Entity(
    tableName = "skills",
    foreignKeys = [
        ForeignKey(
            entity = CharacterEntity::class,
            parentColumns = ["id"],
            childColumns = ["characterOwnerId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("characterOwnerId")]
)
data class SkillEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val characterOwnerId: Long,
    val name: String,
    val isProficient: Boolean
)

@Entity(
    tableName = "attacks",
    foreignKeys = [
        ForeignKey(
            entity = CharacterEntity::class,
            parentColumns = ["id"],
            childColumns = ["characterOwnerId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("characterOwnerId")]
)
data class AttackEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val characterOwnerId: Long,
    val name: String,
    val icon: String,
    val range: String,
    val attackBonusOrSaveDc: String,
    val damage: String,
    val damageType: String
)

@Entity(
    tableName = "combat_resources",
    foreignKeys = [
        ForeignKey(
            entity = CharacterEntity::class,
            parentColumns = ["id"],
            childColumns = ["characterOwnerId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("characterOwnerId")]
)
data class CombatResourceEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val characterOwnerId: Long,
    val name: String,
    val currentUses: Int,
    val maximumUses: Int
)

@Entity(
    tableName = "inventory_items",
    foreignKeys = [
        ForeignKey(
            entity = CharacterEntity::class,
            parentColumns = ["id"],
            childColumns = ["characterOwnerId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("characterOwnerId")]
)
data class InventoryItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val characterOwnerId: Long,
    val name: String,
    val category: InventoryCategory,
    val weight: Double,
    val quantity: Int,
    val isEquipped: Boolean,
    val icon: String
)

@Entity(
    tableName = "spells",
    foreignKeys = [
        ForeignKey(
            entity = CharacterEntity::class,
            parentColumns = ["id"],
            childColumns = ["characterOwnerId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("characterOwnerId")]
)
data class SpellEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val characterOwnerId: Long,
    val name: String,
    val level: Int,
    val school: String,
    val isPrepared: Boolean,
    val description: String
)

@Entity(
    tableName = "features",
    foreignKeys = [
        ForeignKey(
            entity = CharacterEntity::class,
            parentColumns = ["id"],
            childColumns = ["characterOwnerId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("characterOwnerId")]
)
data class FeatureEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val characterOwnerId: Long,
    val name: String,
    val description: String,
    val resourceTracking: String?
)

@Entity(
    tableName = "notes",
    foreignKeys = [
        ForeignKey(
            entity = CharacterEntity::class,
            parentColumns = ["id"],
            childColumns = ["characterOwnerId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("characterOwnerId")]
)
data class NoteEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val characterOwnerId: Long,
    val title: String,
    val createdDate: Long,
    val updatedDate: Long,
    val content: String
)

data class CharacterWithDetails(
    @Embedded val character: CharacterEntity,
    @Relation(parentColumn = "id", entityColumn = "characterOwnerId")
    val skills: List<SkillEntity>,
    @Relation(parentColumn = "id", entityColumn = "characterOwnerId")
    val attacks: List<AttackEntity>,
    @Relation(parentColumn = "id", entityColumn = "characterOwnerId")
    val combatResources: List<CombatResourceEntity>,
    @Relation(parentColumn = "id", entityColumn = "characterOwnerId")
    val inventoryItems: List<InventoryItemEntity>,
    @Relation(parentColumn = "id", entityColumn = "characterOwnerId")
    val spells: List<SpellEntity>,
    @Relation(parentColumn = "id", entityColumn = "characterOwnerId")
    val features: List<FeatureEntity>,
    @Relation(parentColumn = "id", entityColumn = "characterOwnerId")
    val notes: List<NoteEntity>
)

