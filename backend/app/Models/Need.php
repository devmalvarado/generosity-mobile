<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Builder;
use Illuminate\Database\Eloquent\Model;
use Illuminate\Database\Eloquent\Relations\HasMany;

class Need extends Model
{
    public $incrementing = false;

    protected $keyType = 'string';

    protected $guarded = [];

    protected $casts = ['details' => 'array', 'expires_on' => 'date:Y-m-d'];

    public function contributions(): HasMany
    {
        return $this->hasMany(Contribution::class);
    }

    public function scopeWithProgress(Builder $query): void
    {
        $query->withSum(['contributions as quantity_received' => fn ($query) => $query->where('method', 'goods')->where('status', 'fulfilled')], 'quantity');
    }

    public function publicData(): array
    {
        if (! array_key_exists('quantity_received', $this->getAttributes())) {
            $this->loadSum(['contributions as quantity_received' => fn ($query) => $query->where('method', 'goods')->where('status', 'fulfilled')], 'quantity');
        }

        return array_merge($this->details, [
            'id' => $this->id, 'centerId' => $this->center_id,
            'status' => $this->status, 'version' => $this->version,
            'quantityRequired' => $this->quantity_required,
            'quantityCommitted' => $this->quantity_committed,
            'quantityReceived' => (int) $this->quantity_received,
            'expiresOnIsoDate' => $this->expires_on?->toDateString(),
            'updatedAt' => $this->updated_at->toISOString(),
        ]);
    }
}
